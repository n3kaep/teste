package com.veil.client;

import com.veil.Veil;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Veil.ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
	private ClientForgeEvents() {}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.END) ClientState.tick();
	}
}
