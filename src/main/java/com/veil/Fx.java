package com.veil;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class Fx {
	private Fx() {}

	public static void soundTo(ServerPlayer p, SoundEvent s, SoundSource cat, Vec3 at, float vol, float pitch) {
		p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(s), cat,
				at.x, at.y, at.z, vol, pitch, ThreadLocalRandom.current().nextLong()));
	}

	public static boolean isLookingAt(Player p, Entity e, double cos) {
		Vec3 to = e.getEyePosition().subtract(p.getEyePosition()).normalize();
		return p.getViewVector(1.0f).dot(to) > cos && p.hasLineOfSight(e);
	}

	public static Vec3 findSpot(ServerPlayer p, double min, double max, double offDeg, double spreadDeg) {
		ServerLevel w = p.serverLevel();
		ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < 40; i++) {
			double d = min + r.nextDouble() * (max - min);
			double yaw = p.getYRot() + offDeg + (r.nextDouble() * 2 - 1) * spreadDeg;
			Vec3 dir = Vec3.directionFromRotation(0, (float) yaw);
			int bx = Mth.floor(p.getX() + dir.x * d);
			int bz = Mth.floor(p.getZ() + dir.z * d);
			if (!w.hasChunkAt(new BlockPos(bx, 0, bz))) continue;
			int y0 = p.getBlockY();
			for (int k = 0; k < 19; k++) {
				int dy = (k % 2 == 0) ? -k / 2 : (k + 1) / 2;
				BlockPos m = new BlockPos(bx, y0 + dy, bz);
				BlockPos g = m.below();
				if (w.getBlockState(m).isAir()
						&& w.getBlockState(m.above()).isAir()
						&& w.getBlockState(m.above(2)).isAir()
						&& w.getFluidState(g).isEmpty()
						&& w.getBlockState(g).isFaceSturdy(w, g, Direction.UP)) {
					return new Vec3(bx + 0.5, m.getY(), bz + 0.5);
				}
			}
		}
		return null;
	}
}
