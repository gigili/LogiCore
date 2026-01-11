package dev.gacbl.logicore.blocks.compiler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.gacbl.logicore.LogiCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CompilerBlockEntityRenderer implements BlockEntityRenderer<CompilerBlockEntity> {
    private static final ResourceLocation LASER_TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/block/compiler_laser.png");

    public CompilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CompilerBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getLevel() == null) return;
        if (pBlockEntity.getItemHandler(null).getStackInSlot(CompilerBlockEntity.INPUT_SLOT).isEmpty()) return;

        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.hasProperty(CompilerBlock.FACING) ? state.getValue(CompilerBlock.FACING) : Direction.NORTH;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.5f, 0.5f);
        applyFaceRotation(pPoseStack, dir);

        ItemStack stack = pBlockEntity.getItemHandler(null).getStackInSlot(CompilerBlockEntity.INPUT_SLOT);
        if (!stack.isEmpty()) {
            renderFloatingItem(pBlockEntity, stack, pPoseStack, pBufferSource, pPartialTick);
        }

        pPoseStack.popPose();

        /*if (pBlockEntity.getItemHandler(null).getStackInSlot(CompilerBlockEntity.OUTPUT_SLOT).getCount() >= pBlockEntity.getItemHandler(null).getStackInSlot(CompilerBlockEntity.OUTPUT_SLOT).getMaxStackSize()) {
            return;
        }
        if (pBlockEntity.isWorking()) {
            renderMovingLaser(pBlockEntity, pPartialTick, pPoseStack, pBufferSource);
        }*/

    }

    private void renderMovingLaser(CompilerBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();

        long rawTime = tile.getLevel().getGameTime() % 24000;
        float time = rawTime + partialTick;
        float bobHeight = Mth.sin(time * 0.1f) * 0.28f;
        float spinAngle = time * 2.0f;

        poseStack.translate(0, bobHeight, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(spinAngle));

        float size = 0.5f;
        float min = -size / 2;
        float max = size / 2;

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(LASER_TEXTURE));

        PoseStack.Pose currentPose = poseStack.last();
        Matrix4f mat = currentPose.pose();
        Matrix3f nor = currentPose.normal();

        float h = 0.001f;
        vertex(builder, mat, nor, min, h, min, 0, 0);
        vertex(builder, mat, nor, min, h, max, 0, 1);
        vertex(builder, mat, nor, max, h, max, 1, 1);
        vertex(builder, mat, nor, max, h, min, 1, 0);

        vertex(builder, mat, nor, max, -h, min, 1, 0);
        vertex(builder, mat, nor, max, -h, max, 1, 1);
        vertex(builder, mat, nor, min, -h, max, 0, 1);
        vertex(builder, mat, nor, min, -h, min, 0, 0);

        poseStack.popPose();
    }

    private void vertex(VertexConsumer builder, Matrix4f mat, Matrix3f nor, float x, float y, float z, float u, float v) {
        Vector3f normal = new Vector3f(0.0f, 1.0f, 0.0f);
        nor.transform(normal);

        builder.addVertex(mat, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal.x, normal.y, normal.z);
    }

    private void renderFloatingItem(CompilerBlockEntity pBlockEntity, ItemStack stack, PoseStack pPoseStack, MultiBufferSource pBufferSource, float pPartialTick) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Level level = pBlockEntity.getLevel();
        boolean isBlockItem = stack.getItem() instanceof BlockItem;
        float scale = isBlockItem ? 0.5f : 0.35f;
        int lightLevel = getLightLevel(level, pBlockEntity.getBlockPos());

        pPoseStack.pushPose();

        pPoseStack.scale(scale, scale, scale);

        float time = level.getGameTime() + pPartialTick;
        float spinAngle = time * 2.0f;
        pPoseStack.mulPose(Axis.YP.rotationDegrees(spinAngle));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, level, 1);
        pPoseStack.popPose();
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
