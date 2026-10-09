package com.veil;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Veil.ID)
public class Veil {
	public static final String ID = "veil";

	public Veil() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		VeilEntities.ENTITIES.register(bus);
		bus.addListener(Veil::attributes);
		bus.addListener(Veil::setup);
	}

	private static void attributes(EntityAttributeCreationEvent e) {
		e.put(VeilEntities.STALKER.get(), StalkerEntity.attrs().build());
	}

	private static void setup(FMLCommonSetupEvent e) {
		e.enqueueWork(Net::init);
	}
}
