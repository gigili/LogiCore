package dev.gacbl.logicore.blocks.research_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class ResearchStationBlockEntityRenderer implements BlockEntityRenderer<ResearchStationBlockEntity, ResearchStationRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ResearchStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NotNull ResearchStationRenderState createRenderState() {
        return new ResearchStationRenderState();
    }

    @Override
    public void extractRenderState(
            @NotNull ResearchStationBlockEntity blockEntity,
            @NotNull ResearchStationRenderState state,
            float partialTicks,
            @NotNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);

        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.getValue(ResearchStationBlock.FACING);

        var handler = blockEntity.getItemHandler();
        var stack = handler.getResource(0).toStack();
        state.isBlockItem = stack.getItem() instanceof BlockItem;
        itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
    }

    @Override
    public void submit(@NotNull ResearchStationRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        if (state.item.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

        float height = state.isBlockItem ? 1.21f : 1.1f;
        poseStack.translate(0.0, height, 0.1f);
        poseStack.mulPose(Axis.XP.rotationDegrees(23f));
        poseStack.translate(0.0, 0.05, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        float scale = state.isBlockItem ? 0.5f : 0.35f;
        poseStack.scale(scale, scale, scale);

        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
