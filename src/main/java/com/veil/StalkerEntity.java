package com.veil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StalkerEntity extends PathfinderMob {
	private int life;
	private int seen;

	public StalkerEntity(EntityType<? extends StalkerEntity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder attrs() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, 0.14)
				.add(Attributes.FOLLOW_RANGE, 64.0);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) return;
		ServerLevel sw = (ServerLevel) this.level();

		Player near = sw.getNearestPlayer(this, 96);
		if (!(near instanceof ServerPlayer target) || target.isSpectator()) {
			this.discard();
			return;
		}

		life++;
		double dist = this.distanceTo(target);
		if (life > 3000 || dist > 90) {
			this.discard();
			return;
		}

		this.getLookControl().setLookAt(target, 40f, 40f);

		if (Fx.isLookingAt(target, this, 0.86)) {
			seen++;
			this.getNavigation().stop();
			if (seen > (dist < 14 ? 8 : 40)) vanish(target);
			return;
		} else if (seen > 0) {
			seen--;
		}

		if (dist <= 2.2) {
			reach(target);
			return;
		}

		if (this.tickCount % 8 == 0) {
			this.getNavigation().moveTo(target, dist > 20 ? 1.15 : 0.9);
		}
	}

	private void vanish(ServerPlayer target) {
		ServerLevel sw = (ServerLevel) this.level();
		sw.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.5), getZ(), 14, 0.2, 0.6, 0.2, 0.01);
		Fx.soundTo(target, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, position(), 0.7f, 0.4f);
		this.discard();
	}

	private void reach(ServerPlayer target) {
		Net.sendScare(target);
		Fx.soundTo(target, SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, target.position(), 1.6f, 0.45f);
		Fx.soundTo(target, SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, target.position(), 1.2f, 0.5f);
		target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, false, false));
		target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, false));
		Director.bump(target, 6f);
		this.discard();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
			return super.hurt(source, amount);
		}
		if (!this.level().isClientSide && source.getEntity() instanceof ServerPlayer p) {
			vanish(p);
		}
		return false;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}
}
