package com.veil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = Veil.ID)
public final class Director {
	public static final class State {
		public float dread = 4f;
		public int grace = 360;
		public int cooldown = 90;
		public int tick;
		public int beat;
		public int markers;
		public UUID stalker;
	}

	private record Entry(String name, int min, int weight, Events.Ev ev) {}

	private static final List<Entry> TABLE = List.of(
			new Entry("steps", 6, 14, Events::steps),
			new Entry("ambient", 6, 22, Events::ambient),
			new Entry("door", 10, 10, Events::door),
			new Entry("chat", 20, 8, Events::chat),
			new Entry("snuff", 25, 10, Events::snuff),
			new Entry("dark", 30, 8, Events::dark),
			new Entry("stalker", 30, 14, (p, s) -> Events.stalker(p, s, false)),
			new Entry("marker", 35, 6, Events::marker),
			new Entry("whisper", 40, 6, Events::whisper),
			new Entry("behind", 70, 9, (p, s) -> Events.stalker(p, s, true))
	);

	private static final Map<UUID, State> STATES = new HashMap<>();

	private Director() {}

	public static State get(ServerPlayer p) {
		return STATES.computeIfAbsent(p.getUUID(), k -> new State());
	}

	public static void set(ServerPlayer p, float v) {
		get(p).dread = Mth.clamp(v, 0f, 100f);
		Net.sendDread(p, get(p).dread);
	}

	public static void bump(ServerPlayer p, float v) {
		State s = get(p);
		s.dread = Mth.clamp(s.dread + v, 0f, 100f);
		Net.sendDread(p, s.dread);
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent e) {
		if (e.phase != TickEvent.Phase.END) return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) return;
		Scheduler.tick();
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			State s = get(p);
			if (++s.tick >= 20) {
				s.tick = 0;
				try {
					second(p, s);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
		STATES.remove(e.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
		if (!(e.getEntity() instanceof ServerPlayer p)) return;
		State s = get(p);
		s.dread = Math.min(s.dread, 15f);
		s.grace = 120;
		s.stalker = null;
		Net.sendDread(p, s.dread);
	}

	private static void second(ServerPlayer p, State s) {
		ServerLevel w = p.serverLevel();

		if (w.dimension() != Level.OVERWORLD || p.isSpectator()) {
			s.dread = Math.max(0f, s.dread - 0.05f);
			Net.sendDread(p, s.dread);
			return;
		}

		BlockPos pos = p.blockPosition();
		int light = w.getMaxLocalRawBrightness(pos);
		boolean sky = w.canSeeSky(pos);
		boolean night = w.isNight();
		boolean alone = w.getPlayers(o -> o != p && o.distanceToSqr(p) < 48 * 48).isEmpty();

		float d = 0.02f;
		if (night) d += 0.06f;
		if (light <= 4) d += 0.10f;
		if (!sky && pos.getY() < 40) d += 0.06f;
		if (alone) d += 0.03f;
		if (light >= 12) d -= 0.05f;
		if (!night && sky && light >= 12) d = -0.15f;

		s.dread = Mth.clamp(s.dread + d, 0f, 100f);

		if (s.grace > 0) {
			s.grace--;
		} else if (--s.cooldown <= 0 && s.dread >= 6f) {
			fireRandom(p, s);
			s.cooldown = Math.max(18, (int) (75 - s.dread * 0.55f)) + ThreadLocalRandom.current().nextInt(25);
		}

		if (s.dread > 55f && s.grace <= 0) {
			int every = s.dread > 80f ? 1 : 2;
			if (s.beat++ % every == 0) {
				Fx.soundTo(p, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, p.position(), 0.7f, 1.0f);
			}
		}

		Net.sendDread(p, s.dread);
	}

	private static void fireRandom(ServerPlayer p, State s) {
		int total = 0;
		for (Entry e : TABLE) if (s.dread >= e.min()) total += e.weight();
		if (total == 0) return;
		int roll = ThreadLocalRandom.current().nextInt(total);
		for (Entry e : TABLE) {
			if (s.dread < e.min()) continue;
			roll -= e.weight();
			if (roll < 0) {
				if (!safe(e.ev(), p, s)) safe(Events::ambient, p, s);
				return;
			}
		}
	}

	private static boolean safe(Events.Ev ev, ServerPlayer p, State s) {
		try {
			return ev.run(p, s);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean fireNamed(ServerPlayer p, String name) {
		for (Entry e : TABLE) {
			if (e.name().equalsIgnoreCase(name)) return safe(e.ev(), p, get(p));
		}
		return false;
	}
}
