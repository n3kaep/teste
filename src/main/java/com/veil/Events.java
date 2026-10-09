package com.veil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

public final class Events {
	public interface Ev {
		boolean run(ServerPlayer p, Director.State s);
	}

	private record Cue(SoundEvent s, double min, double max, float p0, float p1, float vol, boolean behind) {}

	private static final Cue[] CUES = {
			new Cue(SoundEvents.CREEPER_PRIMED, 2.5, 4, 0.9f, 1.0f, 1.0f, true),
			new Cue(SoundEvents.PLAYER_BREATH, 0.8, 1.5, 0.7f, 0.9f, 1.3f, true),
			new Cue(SoundEvents.CHEST_OPEN, 5, 10, 0.8f, 1.0f, 1.0f, false),
			new Cue(SoundEvents.CHEST_CLOSE, 5, 10, 0.8f, 1.0f, 1.0f, false),
			new Cue(SoundEvents.ZOMBIE_AMBIENT, 12, 20, 0.5f, 0.7f, 1.0f, false),
			new Cue(SoundEvents.SKELETON_AMBIENT, 10, 18, 0.6f, 0.8f, 1.0f, false),
			new Cue(SoundEvents.ENDERMAN_STARE, 10, 16, 0.6f, 0.8f, 0.9f, false),
			new Cue(SoundEvents.VILLAGER_NO, 10, 18, 0.5f, 0.6f, 1.0f, false),
			new Cue(SoundEvents.BELL_BLOCK, 18, 30, 0.5f, 0.6f, 1.5f, false),
			new Cue(SoundEvents.PHANTOM_AMBIENT, 8, 14, 0.6f, 0.8f, 1.0f, false),
			new Cue(SoundEvents.WARDEN_NEARBY_CLOSE, 6, 10, 0.8f, 1.0f, 1.0f, false),
			new Cue(SoundEvents.WOODEN_DOOR_CLOSE, 6, 12, 0.8f, 1.0f, 1.0f, false)
	};

	private static final String[] FAKE = { "St3v3", "Nu11", "Vazio", "player_0", "Ninguem" };

	private static final String[] LINES = {
			"não olha pra trás",
			"eu tô te vendo, %s",
			"%s. %s. %s.",
			"bonita casa",
			"você devia ter dormido",
			"tem alguém aí contigo",
			"%s, você trancou a porta?",
			"para de cavar",
			"eu estava aqui antes de você",
			"quase lá"
	};

	private static final String[] WHISPER = {
			"ele sabe onde você dorme",
			"não corra",
			"continue andando",
			"já é tarde",
			"atrás",
			"fique parado"
	};

	private Events() {}

	private static ThreadLocalRandom r() {
		return ThreadLocalRandom.current();
	}

	public static boolean steps(ServerPlayer p, Director.State s) {
		ServerLevel w = p.serverLevel();
		Vec3 look = Vec3.directionFromRotation(0, p.getYRot());
		int n = 5 + r().nextInt(5);
		for (int i = 0; i < n; i++) {
			final int k = i;
			Scheduler.after(i * 9 + r().nextInt(3), () -> {
				if (p.isRemoved()) return;
				double d = Math.max(2.5, 9.0 - k * 0.9);
				Vec3 at = p.position().subtract(look.scale(d));
				BlockPos below = BlockPos.containing(at.x, p.getY() - 1, at.z);
				BlockState bs = w.getBlockState(below);
				SoundEvent step = bs.isAir() ? SoundEvents.STONE_STEP : bs.getSoundType().getStepSound();
				Fx.soundTo(p, step, SoundSource.HOSTILE, at, 0.9f, 0.85f);
			});
		}
		return true;
	}

	public static boolean ambient(ServerPlayer p, Director.State s) {
		Cue c = CUES[r().nextInt(CUES.length)];
		Vec3 look = Vec3.directionFromRotation(0, p.getYRot());
		double d = c.min() + r().nextDouble() * (c.max() - c.min());
		Vec3 at;
		if (c.behind()) {
			at = p.getEyePosition().subtract(look.scale(d));
		} else {
			double a = r().nextDouble() * Math.PI * 2;
			at = p.getEyePosition().add(Math.cos(a) * d, 0, Math.sin(a) * d);
		}
		Fx.soundTo(p, c.s(), SoundSource.HOSTILE, at, c.vol(), c.p0() + r().nextFloat() * (c.p1() - c.p0()));
		return true;
	}

	public static boolean door(ServerPlayer p, Director.State s) {
		ServerLevel w = p.serverLevel();
		BlockPos c = p.blockPosition();
		for (int i = 0; i < 80; i++) {
			BlockPos q = c.offset(r().nextInt(-14, 15), r().nextInt(-4, 5), r().nextInt(-14, 15));
			BlockState st = w.getBlockState(q);
			if (st.getBlock() instanceof DoorBlock d
					&& st.getBlock() != Blocks.IRON_DOOR
					&& st.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
				d.setOpen(null, w, st, q, !st.getValue(DoorBlock.OPEN));
				return true;
			}
		}
		return false;
	}

	public static boolean chat(ServerPlayer p, Director.State s) {
		String me = p.getName().getString();
		String fake = FAKE[r().nextInt(FAKE.length)];
		String line = LINES[r().nextInt(LINES.length)].replace("%s", me);
		boolean join = r().nextInt(100) < 45;
		if (join) {
			p.sendSystemMessage(Component.literal(fake + " joined the game").withStyle(ChatFormatting.YELLOW));
		}
		Scheduler.after(join ? 50 + r().nextInt(60) : 0, () -> {
			if (p.isRemoved()) return;
			p.sendSystemMessage(Component.literal("<" + fake + "> " + line));
			if (join) {
				Scheduler.after(30 + r().nextInt(40), () -> {
					if (!p.isRemoved()) {
						p.sendSystemMessage(Component.literal(fake + " left the game").withStyle(ChatFormatting.YELLOW));
					}
				});
			}
		});
		return true;
	}

	public static boolean snuff(ServerPlayer p, Director.State s) {
		ServerLevel w = p.serverLevel();
		BlockPos c = p.blockPosition();
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos q : BlockPos.betweenClosed(c.offset(-12, -5, -12), c.offset(12, 5, 12))) {
			BlockState st = w.getBlockState(q);
			Block b = st.getBlock();
			if (b == Blocks.TORCH || b == Blocks.WALL_TORCH || b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH
					|| (b == Blocks.CAMPFIRE && st.getValue(CampfireBlock.LIT))) {
				found.add(q.immutable());
			}
		}
		if (found.isEmpty()) return false;
		Collections.shuffle(found);
		int n = Math.min(found.size(), 1 + r().nextInt(3));
		for (int i = 0; i < n; i++) {
			BlockPos q = found.get(i);
			BlockState st = w.getBlockState(q);
			if (st.getBlock() == Blocks.CAMPFIRE) {
				w.setBlockAndUpdate(q, st.setValue(CampfireBlock.LIT, false));
			} else {
				w.setBlockAndUpdate(q, Blocks.AIR.defaultBlockState());
			}
			final Vec3 at = Vec3.atCenterOf(q);
			Scheduler.after(i * 12, () -> Fx.soundTo(p, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, at, 1.0f, 0.8f));
		}
		return true;
	}

	public static boolean dark(ServerPlayer p, Director.State s) {
		p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0, false, false, false));
		if (s.dread > 60) {
			p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, false));
		}
		Fx.soundTo(p, SoundEvents.WARDEN_NEARBY_CLOSE, SoundSource.HOSTILE, p.position(), 0.8f, 0.7f);
		return true;
	}

	public static boolean marker(ServerPlayer p, Director.State s) {
		if (s.markers >= 5) return false;
		Vec3 spot = Fx.findSpot(p, 10, 22, 180, 70);
		if (spot == null) return false;
		ServerLevel w = p.serverLevel();
		BlockPos b = BlockPos.containing(spot.x, spot.y, spot.z);
		w.setBlockAndUpdate(b, Blocks.NETHERRACK.defaultBlockState());
		w.setBlockAndUpdate(b.above(), Blocks.NETHERRACK.defaultBlockState());
		w.setBlockAndUpdate(b.above(2), Blocks.REDSTONE_TORCH.defaultBlockState());
		s.markers++;
		return true;
	}

	public static boolean whisper(ServerPlayer p, Director.State s) {
		String line = WHISPER[r().nextInt(WHISPER.length)];
		p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)
				.append(Component.literal(" ||||").withStyle(ChatFormatting.DARK_RED, ChatFormatting.OBFUSCATED)), true);
		return true;
	}

	public static boolean stalker(ServerPlayer p, Director.State s, boolean close) {
		ServerLevel w = p.serverLevel();
		if (s.stalker != null && w.getEntity(s.stalker) != null) return false;
		Vec3 spot = close ? Fx.findSpot(p, 5, 8, 180, 50) : Fx.findSpot(p, 22, 34, 0, 45);
		if (spot == null) return false;
		StalkerEntity e = VeilEntities.STALKER.get().create(w);
		if (e == null) return false;
		double dx = p.getX() - spot.x;
		double dz = p.getZ() - spot.z;
		float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
		e.moveTo(spot.x, spot.y, spot.z, yaw, 0f);
		e.setYHeadRot(yaw);
		e.yBodyRot = yaw;
		w.addFreshEntity(e);
		s.stalker = e.getUUID();
		return true;
	}
}
