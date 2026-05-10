package dev.gacbl.logicore.blocks.repair_station;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.NotNull;

public class RepairStationBlockEntityRenderer implements BlockEntityRenderer<RepairStationBlockEntity, BlockEntityRenderState> {

    public RepairStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(@NotNull BlockEntityRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState camera) {
        // TODO: Port repair station rendering to new 26.1 API (SubmitNodeCollector-based)
        // Previously rendered: repair item, text info, laser beam, molecular particles, fill bar
    }
}
