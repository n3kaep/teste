package com.veil.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.veil.StalkerEntity;
import com.veil.Veil;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class StalkerRenderer extends MobRenderer<StalkerEntity, HumanoidModel<StalkerEntity>> {
	private static final ResourceLocation TEX = new ResourceLocation(Veil.ID, "textures/entity/stalker.png");
	private static final ResourceLocation EYES = new ResourceLocation(Veil.ID, "textures/entity/stalker_eyes.png");

	public StalkerRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.0f);
		this.addLayer(new Eyes(this));
	}

	@Override
	public ResourceLocation getTextureLocation(StalkerEntity entity) {
		return TEX;
	}

	@Override
	protected void scale(StalkerEntity entity, PoseStack stack, float partialTick) {
		stack.scale(0.92f, 1.28f, 0.92f);
	}

	private static final class Eyes extends EyesLayer<StalkerEntity, HumanoidModel<StalkerEntity>> {
		Eyes(RenderLayerParent<StalkerEntity, HumanoidModel<StalkerEntity>> parent) {
			super(parent);
		}

		@Override
		public RenderType renderType() {
			return RenderType.eyes(EYES);
		}
	}
}
