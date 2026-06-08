package dev.gacbl.logicore.blocks.compiler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gacbl.logicore.LogiCore;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CompilerBlockEntityRenderer implements BlockEntityRenderer<CompilerBlockEntity, CompilerBlockRenderState> {
    private static final Identifier LASER_TEXTURE = Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/block/compiler_laser.png");
    private final ItemModelResolver itemModelResolver;

    public CompilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NotNull CompilerBlockRenderState createRenderState() {
        return new CompilerBlockRenderState();
    }

    @Override
    public void extractRenderState(
            @NotNull CompilerBlockEntity blockEntity,
            @NotNull CompilerBlockRenderState state,
            float partialTicks,
            @NotNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);

        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.hasProperty(CompilerBlock.FACING) ? blockState.getValue(CompilerBlock.FACING) : Direction.NORTH;

        var handler = blockEntity.getInternalItemHandler();
        itemModelResolver.updateForTopItem(state.item, handler.getResource(CompilerBlockEntity.INPUT_SLOT).toStack(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);

        state.isWorking = blockEntity.isWorking();
        state.renderRotation = blockEntity.getRenderingRotation();
        state.progress = blockEntity.getProgress(partialTicks);
        if (blockEntity.getLevel() != null) {
            state.time = blockEntity.getLevel().getGameTime();
        }
    }

    @Override
    public void submit(@NotNull CompilerBlockRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraRenderState) {
        if (state.item.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        applyFaceRotation(poseStack, state.facing);

        float scale = 0.4f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.renderRotation));
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
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
}
