package com.asbjborg.climp.client;

import com.asbjborg.climp.client.model.ClimpModel;
import com.asbjborg.climp.entity.ClimpEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Climp's custom geometric model.
 */
public class ClimpEntityRenderer extends MobRenderer<ClimpEntity, ClimpModel> {
    private static final ResourceLocation CLIMP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/iron_block.png");

    public ClimpEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ClimpModel(context.bakeLayer(ClimpModel.LAYER_LOCATION)), 0.75F);
    }

    @Override
    public ResourceLocation getTextureLocation(ClimpEntity entity) {
        return CLIMP_TEXTURE;
    }

    @Override
    protected void scale(ClimpEntity entity, PoseStack poseStack, float partialTickTime) {
        // Make Climp clearly larger while we iterate on final proportions.
        poseStack.scale(1.55F, 1.55F, 1.55F);
    }
}
