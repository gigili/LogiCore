package dev.gacbl.logicore.blocks.compiler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CompilerBlockEntityRenderer implements BlockEntityRenderer<CompilerBlockEntity> {

    public CompilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CompilerBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getLevel() == null) return;

        ItemStack stack = pBlockEntity.getItemHandler(null).getStackInSlot(CompilerBlockEntity.INPUT_SLOT);
        if (stack.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BlockPos pos = pBlockEntity.getBlockPos();
        Level level = pBlockEntity.getLevel();

        boolean isBlockItem = stack.getItem() instanceof BlockItem;
        float scale = isBlockItem ? 0.5f : 0.35f;

        for (Direction dir : Direction.values()) {

            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.isFaceSturdy(level, neighborPos, dir.getOpposite()) || !neighborState.isAir()) {
                continue;
            }

            int lightLevel = getLightLevel(level, pos.relative(dir));
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 0.5f, 0.5f);

            float offset = 0.501f;
            pPoseStack.translate(dir.getStepX() * offset, dir.getStepY() * offset, dir.getStepZ() * offset);
            applyFaceRotation(pPoseStack, dir);
            pPoseStack.scale(scale, scale, scale);

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, level, 1);

            pPoseStack.popPose();
        }
    }

    private void applyFaceRotation(PoseStack poseStack, Direction face) {
        switch (face) {
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        if (face == Direction.UP) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
