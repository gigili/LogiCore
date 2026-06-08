package dev.gacbl.logicore.blocks.repair_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class RepairStationBlockEntityRenderer implements BlockEntityRenderer<RepairStationBlockEntity, RepairStationRenderState> {
    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public RepairStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        font = context.font();
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NotNull RepairStationRenderState createRenderState() {
        return new RepairStationRenderState();
    }

    @Override
    public void extractRenderState(
            @NotNull RepairStationBlockEntity blockEntity,
            @NotNull RepairStationRenderState state,
            float partialTicks,
            @NotNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);

        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.getValue(RepairStationBlock.FACING);
        state.isRepairing = blockState.getValue(RepairStationModule.REPAIRING);
        state.blockPos = blockEntity.getBlockPos();

        var handler = blockEntity.getItemHandler();
        ItemStack stack = handler.getResource(0).toStack();
        state.hasStack = !stack.isEmpty();
        state.itemName = stack.getHoverName().getString();
        state.damageValue = stack.getDamageValue();
        state.maxDamage = stack.getMaxDamage();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();

        state.isBlockItem = stack.getItem() instanceof BlockItem;
        state.partialTick = partialTicks;

        itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);

        if (blockEntity.getLevel() != null) {
            state.gameTime = blockEntity.getLevel().getGameTime();
        }
    }

    @Override
    public void submit(@NotNull RepairStationRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        submitItem(state, poseStack, collector);
        submitInformation(state, poseStack, collector);
        if (state.hasStack && state.isRepairing) {
            submitLaser(state, poseStack, collector);
        }
    }

    private void submitItem(RepairStationRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.item.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

        float height = state.isBlockItem ? 0.83f : 0.73f;
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

    private void submitInformation(RepairStationRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!state.hasStack) {
            Component text = Component.literal("Insert broken item to repair it");
            renderText(text, -0.03, state.facing, poseStack, collector, 0, state.lightCoords);
            return;
        }

        if (state.damageValue > 0) {
            Component repairLine = Component.literal("Repairing: " + state.itemName);
            renderText(repairLine, 0, state.facing, poseStack, collector, 0, state.lightCoords);

            Component restoredLine = Component.literal("Restored: " + (state.maxDamage - state.damageValue) + " / " + state.maxDamage);
            renderText(restoredLine, -0.05, state.facing, poseStack, collector, 0, state.lightCoords);
        } else {
            Component restoredLine = Component.literal("Restored: " + state.itemName);
            renderText(restoredLine, -0.03, state.facing, poseStack, collector, 0, state.lightCoords);
        }

        submitFillBar(state, poseStack, collector);
    }

    private void submitFillBar(RepairStationRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.item.isEmpty()) return;
        if (state.maxDamage <= 0) return;

        float fillRatio = (float) (state.maxDamage - state.damageValue) / state.maxDamage;

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        TextureAtlas atlas = (TextureAtlas) textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(Identifier.withDefaultNamespace("block/white_concrete"));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.365, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

        float p = 1.0f / 16.0f;
        float width = 9.0f * p;
        float height = 0.3f * p;
        float yOffset = -4.0f * p;

        float xMin = -width / 2.0f;
        float xMax = xMin + (width * fillRatio);

        poseStack.translate(0, 0.13D, 0.58);

        collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pp, buffer) -> {
            addQuad(pp.pose(), buffer, xMin, xMin + width, yOffset, yOffset + height, OverlayTexture.NO_OVERLAY, sprite, 0xff1f50da);
        });

        if (fillRatio > 0) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 0.0001D);
            collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pp, buffer) -> {
                addQuad(pp.pose(), buffer, xMin, xMax, yOffset, yOffset + height, OverlayTexture.NO_OVERLAY, sprite, 0xff1f81da);
            });
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderText(Component text, double y, Direction facing, PoseStack poseStack, SubmitNodeCollector collector, int color, int lightCoords) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.365, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(0, y, 0.58 - 0.002);

        float scale = 0.004f;
        poseStack.scale(scale, -scale, scale);

        List<FormattedCharSequence> lines = font.split(text, 200);
        float lineHeight = 9.0f;
        float totalHeight = lines.size() * lineHeight;
        float yOffset = -totalHeight / 2.0f;

        for (FormattedCharSequence line : lines) {
            float width = font.width(line);
            float xOffset = -width / 2.0f;
            collector.submitText(poseStack, xOffset, yOffset, line, true, Font.DisplayMode.NORMAL, lightCoords, 0xFFFFFFFF, 0, 0);
            yOffset += lineHeight;
        }

        poseStack.popPose();
    }

    private void submitLaser(RepairStationRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

        float time = ((state.gameTime % 40) + state.partialTick) / 40.0f;
        float oscillation = (float) Math.sin(time * Math.PI * 2) * 0.08f;
        float oscillationZ = (float) Math.cos(time * Math.PI * 2) * 0.03f;

        Vector3f start = new Vector3f(0, 1.134f, 0.15f);
        float itemHeight = state.isBlockItem ? 0.83f : 0.73f;
        Vector3f target = new Vector3f(oscillation, itemHeight, 0.1f + oscillationZ);
        float beamLength = 0.32f;
        Vector3f direction = new Vector3f(target).sub(start).normalize(beamLength);
        Vector3f end = new Vector3f(start).add(direction);

        drawLaserBeam(poseStack, collector, start, end);
        drawMolecularParticles(poseStack, collector, end, state.gameTime);

        poseStack.popPose();
    }

    private void drawLaserBeam(PoseStack poseStack, SubmitNodeCollector collector, Vector3f start, Vector3f end) {
        Vector3f diff = new Vector3f(end).sub(start);
        float length = diff.length();

        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);

        float yaw = (float) Math.atan2(diff.x, diff.z);
        float pitch = (float) -Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z));

        poseStack.mulPose(Axis.YP.rotation(yaw));
        poseStack.mulPose(Axis.XP.rotation(pitch));

        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(45f * i));
            collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pp, buffer) -> {
                Matrix4f m = pp.pose();
                buffer.addVertex(m, -(float) 0.03, 0, 0).setColor(-16711681);
                buffer.addVertex(m, (float) 0.03, 0, 0).setColor(-16711681);
                buffer.addVertex(m, (float) 0.03, 0, length).setColor(-16711681);
                buffer.addVertex(m, -(float) 0.03, 0, length).setColor(-16711681);
            });
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void drawMolecularParticles(PoseStack poseStack, SubmitNodeCollector collector, Vector3f position, long gameTime) {
        float size = 0.005f;
        int color = 0xFF00FFFF;

        Random particleRandom = new Random(gameTime / 2);

        for (int i = 0; i < 5; i++) {
            float offX = (particleRandom.nextFloat() - 0.5f) * 0.3f;
            float offY = (particleRandom.nextFloat() - 0.5f) * 0.3f;
            float offZ = (particleRandom.nextFloat() - 0.5f) * 0.3f;

            poseStack.pushPose();
            poseStack.translate(position.x + offX, position.y + offY, position.z + offZ);

            float spin = (gameTime % 20) * 18.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            poseStack.mulPose(Axis.XP.rotationDegrees(spin));

            collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pp, buffer) -> {
                addBox(pp.pose(), buffer, -size, size, -size, size, -size, size, color);
            });

            poseStack.popPose();
        }
    }

    private void addBox(Matrix4f m, VertexConsumer consumer, float x1, float x2, float y1, float y2, float z1, float z2, int color) {
        consumer.addVertex(m, x1, y1, z2).setColor(color);
        consumer.addVertex(m, x2, y1, z2).setColor(color);
        consumer.addVertex(m, x2, y2, z2).setColor(color);
        consumer.addVertex(m, x1, y2, z2).setColor(color);
        consumer.addVertex(m, x1, y1, z1).setColor(color);
        consumer.addVertex(m, x1, y2, z1).setColor(color);
        consumer.addVertex(m, x2, y2, z1).setColor(color);
        consumer.addVertex(m, x2, y1, z1).setColor(color);
        consumer.addVertex(m, x1, y2, z1).setColor(color);
        consumer.addVertex(m, x1, y2, z2).setColor(color);
        consumer.addVertex(m, x2, y2, z2).setColor(color);
        consumer.addVertex(m, x2, y2, z1).setColor(color);
        consumer.addVertex(m, x1, y1, z1).setColor(color);
        consumer.addVertex(m, x2, y1, z1).setColor(color);
        consumer.addVertex(m, x2, y1, z2).setColor(color);
        consumer.addVertex(m, x1, y1, z2).setColor(color);
        consumer.addVertex(m, x2, y1, z1).setColor(color);
        consumer.addVertex(m, x2, y2, z1).setColor(color);
        consumer.addVertex(m, x2, y2, z2).setColor(color);
        consumer.addVertex(m, x2, y1, z2).setColor(color);
        consumer.addVertex(m, x1, y1, z1).setColor(color);
        consumer.addVertex(m, x1, y1, z2).setColor(color);
        consumer.addVertex(m, x1, y2, z2).setColor(color);
        consumer.addVertex(m, x1, y2, z1).setColor(color);
    }

    private void addQuad(Matrix4f pose, VertexConsumer vertexConsumer, float xMin, float xMax, float yMin, float yMax, int overlay, TextureAtlasSprite sprite, int color) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        vertexConsumer.addVertex(pose, xMin, yMin, 0).setColor(color).setUv(u0, v1).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
        vertexConsumer.addVertex(pose, xMax, yMin, 0).setColor(color).setUv(u1, v1).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
        vertexConsumer.addVertex(pose, xMax, yMax, 0).setColor(color).setUv(u1, v0).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
        vertexConsumer.addVertex(pose, xMin, yMax, 0).setColor(color).setUv(u0, v0).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
    }
}
