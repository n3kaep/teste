package com.veil.client;

import com.veil.Veil;
import com.veil.VeilEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Veil.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
	private ClientModEvents() {}

	@SubscribeEvent
	public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
		e.registerEntityRenderer(VeilEntities.STALKER.get(), StalkerRenderer::new);
	}

	@SubscribeEvent
	public static void overlays(RegisterGuiOverlaysEvent e) {
		e.registerAboveAll("dread", ClientState::render);
	}
}
