package dev.gacbl.logicore.entity.drone.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DroneRenderer extends MobRenderer<DroneEntity, DroneModel<DroneEntity>> {
    public DroneRenderer(EntityRendererProvider.Context context) {
        super(context, new DroneModel<>(context.bakeLayer(DroneModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DroneEntity droneEntity) {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/entity/drone.png");
    }

    @Override
    public void render(@NotNull DroneEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
