package dev.gacbl.logicore.blocks.battery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class BatteryFillRenderer implements BlockEntityRenderer<BaseBatteryEntity, BlockEntityRenderState> {

    public BatteryFillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(@NotNull BlockEntityRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState camera) {
        // TODO: Port battery fill rendering to new 26.1 rendering API (SubmitNodeCollector-based)
        // The rendering was moved from the old render() method to the submit() method.
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
