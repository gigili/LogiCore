package dev.gacbl.logicore.blocks.battery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class BatteryFillRenderer implements BlockEntityRenderer<BaseBatteryEntity, BatteryFillRenderState> {

    public BatteryFillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NotNull BatteryFillRenderState createRenderState() {
        return new BatteryFillRenderState();
    }

    @Override
    public void extractRenderState(
            @NotNull BaseBatteryEntity blockEntity,
            @NotNull BatteryFillRenderState state,
            float partialTicks,
            @NotNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        EnergyHandler energyHandler = blockEntity.getEnergyHandler();
        int capacity = energyHandler.getCapacityAsInt();
        if (capacity > 0) {
            state.fillRatio = (float) energyHandler.getAmountAsInt() / capacity;
        }
    }

    @Override
    public void submit(@NotNull BatteryFillRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState camera) {
        if (state.fillRatio <= 0) return;

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        TextureAtlas atlas = (TextureAtlas) textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(Identifier.withDefaultNamespace("block/white_concrete"));

        Level level = Minecraft.getInstance().level;

        for (Direction facing : Direction.values()) {
            if (level != null) {
                BlockPos neighborPos = state.blockPos.relative(facing.getOpposite());
                BlockState neighborState = level.getBlockState(neighborPos);
                if (!neighborState.isAir()) continue;
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

            float filledHeight = height * state.fillRatio;

            float xMin = (-width / 2) + xOffset;
            float xMax = (width / 2) + xOffset;
            float yMaxFilled = yBottomOffset + filledHeight;

            poseStack.translate(0.0D, 0.0D, -0.001D);
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) ->
                    addQuad(pose.pose(), buffer, xMin, xMax, yBottomOffset, yMaxFilled, OverlayTexture.NO_OVERLAY, sprite)
            );

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
