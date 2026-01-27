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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS)
                .apply(ResourceLocation.withDefaultNamespace("block/white_concrete"));

        BlockPos pos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();

        for (Direction facing : Direction.values()) {
            if (level != null) {
                BlockPos neighborPos = pos.relative(facing.getOpposite());
                BlockState neighborState = level.getBlockState(neighborPos);
                if (!neighborState.isAir()) {
                    continue;
                }
            }

            poseStack.pushPose();

            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            poseStack.translate(0.0D, 0.0D, -0.50D);

            float p = 1.0f / 16.0f;
            float width = 2.56f * p;
            float height = 5.54f * p;
            float xOffset = 0.001f;
            float yBottomOffset = -2.77f * p;

            float filledHeight = height * fillRatio;

            float xMin = (-width / 2) + xOffset;
            float xMax = (width / 2) + xOffset;
            float yMaxFilled = yBottomOffset + filledHeight;

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
            Matrix4f pose;

            if (fillRatio > 0) {
                poseStack.translate(0.0D, 0.0D, -0.001D);
                pose = poseStack.last().pose();
                addQuad(pose, vertexConsumer, xMin, xMax, yBottomOffset, yMaxFilled, packedOverlay, sprite);
            }

            poseStack.popPose();
        }
    }

    private void addQuad(Matrix4f pose, VertexConsumer vertexConsumer, float xMin, float xMax, float yMin, float yMax, int overlay, TextureAtlasSprite sprite) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        addVertex(pose, vertexConsumer, xMin, xMax, yMax, overlay, u0, u1, v1);
        addVertex(pose, vertexConsumer, xMax, xMin, yMin, overlay, u1, u0, v0);
    }

    private void addVertex(Matrix4f pose, VertexConsumer vertexConsumer, float xMin, float xMax, float yMax, int overlay, float u0, float u1, float v1) {
        vertexConsumer.addVertex(pose, xMin, yMax, 0)
                .setColor(0xff1fda60)
                .setUv(u0, v1)
                .setOverlay(overlay)
                .setLight(15728880)
                .setNormal(0, 0, -1);

        vertexConsumer.addVertex(pose, xMax, yMax, 0)
                .setColor(0xff1fda60)
                .setUv(u1, v1)
                .setOverlay(overlay)
                .setLight(15728880)
                .setNormal(0, 0, -1);
    }
}
