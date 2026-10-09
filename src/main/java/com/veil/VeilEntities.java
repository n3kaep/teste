package com.veil;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class VeilEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES =
			DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Veil.ID);

	public static final RegistryObject<EntityType<StalkerEntity>> STALKER = ENTITIES.register("stalker",
			() -> EntityType.Builder.of(StalkerEntity::new, MobCategory.MONSTER)
					.sized(0.6f, 2.4f)
					.clientTrackingRange(8)
					.fireImmune()
					.build("stalker"));

	private VeilEntities() {}
}
