package dev.gacbl.logicore.blocks.research_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ResearchStationBlockEntityRenderer implements BlockEntityRenderer<ResearchStationBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ResearchStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ResearchStationBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getLevel() == null) return;
        ItemStack stack = pBlockEntity.getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) return;

        pPoseStack.pushPose();

        // 1. BASE ANCHOR
        pPoseStack.translate(0.5, 0.0, 0.5);

        // 2. FACING ROTATION
        BlockState state = pBlockEntity.getBlockState();
        if (state.hasProperty(ResearchStationBlock.FACING)) {
            Direction facing = state.getValue(ResearchStationBlock.FACING);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        // 3. POSITION
        // Adjusted height slightly to ensure it doesn't clip with the flip
        boolean isBlockItem = stack.getItem() instanceof BlockItem;
        float height = isBlockItem ? 1.21f : 1.1f;
        float forwardOffset = 0.1f;
        pPoseStack.translate(0.0, height, forwardOffset);

        // 4. APPLY SLOPE (22.5f)
        pPoseStack.mulPose(Axis.XP.rotationDegrees(23f));

        // 5. FLIP & ORIENT
        pPoseStack.translate(0.0, 0.05, 0.0); // Anti-Z-fighting

        // CHANGE HERE: Changed 90f to -90f (270f).
        // This flips the item over so the "Face" points Skyward/Player-ward.
        pPoseStack.mulPose(Axis.XP.rotationDegrees(-90f));

        // Rotated Y 180 is likely still needed to ensure the "Top" of the stair/chest is "Up" relative to the slope
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // 6. RENDER
        float scale = isBlockItem ? 0.5f : 0.35f;
        pPoseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 0);

        pPoseStack.popPose();
    }
}
