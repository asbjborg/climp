package com.asbjborg.climp.client;

import com.asbjborg.climp.entity.ClimpEntity;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Temporary renderer for early entity pipeline validation.
 * Reuses vanilla pig model/texture until Climp's custom model is ready.
 */
public class ClimpEntityRenderer extends MobRenderer<ClimpEntity, PigModel<ClimpEntity>> {
    private static final ResourceLocation PIG_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");

    public ClimpEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(ClimpEntity entity) {
        return PIG_TEXTURE;
    }
}
