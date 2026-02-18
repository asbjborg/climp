package com.asbjborg.climp.client.model;

import com.asbjborg.climp.ClimpMod;
import com.asbjborg.climp.entity.ClimpEntity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Climp v0.2: larger, clearer paperclip silhouette.
 */
public class ClimpModel extends HierarchicalModel<ClimpEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ClimpMod.MODID, "climp"),
            "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart eyePlate;
    private final ModelPart clipUpper;
    private final ModelPart clipLower;

    public ClimpModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.eyePlate = root.getChild("eye_plate");
        this.clipUpper = root.getChild("clip_upper");
        this.clipLower = root.getChild("clip_lower");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        root.addOrReplaceChild(
                "eye_plate",
                CubeListBuilder.create()
                        .texOffs(14, 0)
                        .addBox(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 1.0F)
                        .texOffs(14, 4)
                        .addBox(-0.8F, -0.1F, -2.1F, 0.4F, 0.4F, 0.2F)
                        .texOffs(16, 4)
                        .addBox(0.4F, -0.1F, -2.1F, 0.4F, 0.4F, 0.2F),
                PartPose.offset(0.0F, 17.5F, 0.0F));

        // Outer clip loop.
        root.addOrReplaceChild(
                "clip_upper",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-3.5F, -8.0F, -0.5F, 7.0F, 1.0F, 1.0F)
                        .texOffs(0, 14)
                        .addBox(-3.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                        .texOffs(4, 14)
                        .addBox(2.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                        .texOffs(8, 14)
                        .addBox(-3.5F, -1.0F, -0.5F, 7.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        // Inner clip loop.
        root.addOrReplaceChild(
                "clip_lower",
                CubeListBuilder.create()
                        .texOffs(12, 12)
                        .addBox(-2.4F, -6.0F, -0.7F, 4.8F, 1.0F, 1.4F)
                        .texOffs(12, 15)
                        .addBox(-2.4F, -6.0F, -0.7F, 1.0F, 5.0F, 1.4F)
                        .texOffs(17, 15)
                        .addBox(1.4F, -6.0F, -0.7F, 1.0F, 5.0F, 1.4F)
                        .texOffs(21, 15)
                        .addBox(-2.4F, -2.0F, -0.7F, 4.8F, 1.0F, 1.4F),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(ClimpEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float bob = Mth.sin(ageInTicks * 0.15F) * 0.14F;
        float sway = Mth.sin(ageInTicks * 0.10F) * 0.11F;
        float lookYaw = netHeadYaw * ((float) Math.PI / 180.0F) * 0.25F;

        this.body.y = 18.0F + bob * 3.0F;
        this.eyePlate.y = 17.5F + bob * 3.0F;
        this.clipUpper.y = 21.0F + bob * 3.0F;
        this.clipLower.y = 21.0F + bob * 3.0F;

        this.body.zRot = sway * 0.7F;
        this.eyePlate.zRot = sway * 0.9F;
        this.clipUpper.zRot = sway * 1.15F;
        this.clipLower.zRot = sway * 1.35F;

        this.body.yRot = lookYaw;
        this.eyePlate.yRot = lookYaw;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
