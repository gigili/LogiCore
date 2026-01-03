package dev.gacbl.logicore.entity.drone.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DroneModel<T extends DroneEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "drone"), "main");
    private final ModelPart drone;
    private final ModelPart base;

    public DroneModel(ModelPart root) {
        this.drone = root.getChild("drone");
        this.base = this.drone.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition drone = partdefinition.addOrReplaceChild("drone", CubeListBuilder.create(), PartPose.offset(1.5F, 11.5F, 1.0F));

        PartDefinition base = drone.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, 7.5F, -8.0F, 8.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(24, 21).addBox(2.5F, 4.5F, -9.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-6.5F, 4.5F, -9.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 27).addBox(2.5F, 4.5F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 27).addBox(-6.5F, 4.5F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition blade1 = drone.addOrReplaceChild("blade1", CubeListBuilder.create().texOffs(0, 17).addBox(-0.5F, 0.43F, -2.5F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 17).addBox(-2.5F, 0.45F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 4.0F, -8.5F));

        PartDefinition blade2 = drone.addOrReplaceChild("blade2", CubeListBuilder.create().texOffs(12, 17).addBox(-0.5F, 0.43F, -2.5F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 18).addBox(-2.5F, 0.45F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 4.0F, -8.5F));

        PartDefinition blade3 = drone.addOrReplaceChild("blade3", CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, 0.43F, -2.5F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 19).addBox(-2.5F, 0.45F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 4.0F, 6.5F));

        PartDefinition blade4 = drone.addOrReplaceChild("blade4", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.43F, -2.5F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 20).addBox(-2.5F, 0.45F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 4.0F, 6.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int colors) {
        drone.render(poseStack, vertexConsumer, packedLight, packedOverlay, colors);
    }

    @Override
    public void setupAnim(@NotNull DroneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        //this.animateWalk(DroneAnimations.ANIM_FLYING, limbSwing, limbSwingAmount, limbSwingAmount, ageInTicks);
        this.animate(entity.flyingAnimationState, DroneAnimations.ANIM_FLYING, ageInTicks, 1.0F);
        this.animate(entity.idleAnimationState, DroneAnimations.ANIM_IDLE, ageInTicks, 1f);
    }

    @Override
    public @NotNull ModelPart root() {
        return base;
    }
}
