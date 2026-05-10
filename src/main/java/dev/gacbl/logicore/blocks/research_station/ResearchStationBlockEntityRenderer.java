package dev.gacbl.logicore.blocks.research_station;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.NotNull;

public class ResearchStationBlockEntityRenderer implements BlockEntityRenderer<ResearchStationBlockEntity, BlockEntityRenderState> {

    public ResearchStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(@NotNull BlockEntityRenderState renderState, @NotNull PoseStack pPoseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraRenderState) {
    }
}
