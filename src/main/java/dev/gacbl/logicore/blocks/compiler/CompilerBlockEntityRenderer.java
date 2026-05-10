package dev.gacbl.logicore.blocks.compiler;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gacbl.logicore.LogiCore;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CompilerBlockEntityRenderer implements BlockEntityRenderer<CompilerBlockEntity, BlockEntityRenderState> {
    private static final Identifier LASER_TEXTURE = Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/block/compiler_laser.png");

    public CompilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(@NotNull BlockEntityRenderState renderState, @NotNull PoseStack pPoseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraRenderState) {
    }
}
