package dev.gacbl.logicore.blocks.battery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class BatteryFillRenderer implements BlockEntityRenderer<BaseBatteryEntity> {

    public BatteryFillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BaseBatteryEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        IEnergyStorage energyStorage = blockEntity.getEnergyStorage();
        if (energyStorage == null) return;

        int maxEnergy = energyStorage.getMaxEnergyStored();
        if (maxEnergy <= 0) return;

        int currentEnergy = energyStorage.getEnergyStored();
        float fillRatio = (float) currentEnergy / maxEnergy;

        for (Direction facing : Direction.values()) {
            // Fetch a white texture to use as a base for our colored quads
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ResourceLocation.withDefaultNamespace("block/white_concrete"));

            poseStack.pushPose();

            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            poseStack.translate(0.0D, 0.0D, -0.501D);

            // --- GEOMETRY SETTINGS ---
            float p = 1.0f / 16.0f; // Size of 1 pixel

            // Edit these values to fine-tune the position
            float width = 3.52f * p;          // 4 pixels wide
            float height = 7.4f * p;         // 6 pixels high (Reduced size)
            float xOffset = -0.25f * p;        // Move RIGHT by 2 pixels
            float yBottomOffset = -3.7f * p; // Move UP (starts 2 pixels below center instead of 4)

            float filledHeight = height * fillRatio;

            float xMin = (-width / 2) + xOffset;
            float xMax = (width / 2) + xOffset;
            float yMaxFilled = yBottomOffset + filledHeight;
            // -------------------------

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
            Matrix4f pose;

            // 1. Draw Background (Dark Gray)
            //addQuad(pose, vertexConsumer, xMin, xMax, yMin, yMaxFull, 50, 50, 50, 255, packedLight, packedOverlay, sprite);

            // 2. Draw Fill (Green)
            if (fillRatio > 0) {
                poseStack.translate(0.0D, 0.0D, -0.001D);
                pose = poseStack.last().pose();
                addQuad(pose, vertexConsumer, xMin, xMax, yBottomOffset, yMaxFilled, 0, 255, 0, 255, packedLight, packedOverlay, sprite);
            }

            poseStack.popPose();
        }
    }

    private void addQuad(Matrix4f pose, VertexConsumer vertexConsumer, float xMin, float xMax, float yMin, float yMax, int r, int g, int b, int a, int light, int overlay, TextureAtlasSprite sprite) {
        // Use the sprite's UVs (min/max) to map the texture
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        vertexConsumer.addVertex(pose, xMin, yMax, 0)
                .setColor(r, g, b, a)
                .setUv(u0, v1)
                .setOverlay(overlay) // Added Overlay
                .setLight(light)
                .setNormal(0, 0, -1);

        vertexConsumer.addVertex(pose, xMax, yMax, 0)
                .setColor(r, g, b, a)
                .setUv(u1, v1)
                .setOverlay(overlay) // Added Overlay
                .setLight(light)
                .setNormal(0, 0, -1);

        vertexConsumer.addVertex(pose, xMax, yMin, 0)
                .setColor(r, g, b, a)
                .setUv(u1, v0)
                .setOverlay(overlay) // Added Overlay
                .setLight(light)
                .setNormal(0, 0, -1);

        vertexConsumer.addVertex(pose, xMin, yMin, 0)
                .setColor(r, g, b, a)
                .setUv(u0, v0)
                .setOverlay(overlay) // Added Overlay
                .setLight(light)
                .setNormal(0, 0, -1);
    }
}
