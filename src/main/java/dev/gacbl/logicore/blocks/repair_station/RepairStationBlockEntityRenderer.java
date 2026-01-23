package dev.gacbl.logicore.blocks.repair_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class RepairStationBlockEntityRenderer implements BlockEntityRenderer<RepairStationBlockEntity> {
    private final Font font;
    private final ItemRenderer itemRenderer;
    private BlockState blockState;

    public RepairStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(RepairStationBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getLevel() == null) return;
        this.blockState = pBlockEntity.getBlockState();

        ItemStack stack = pBlockEntity.getItemHandler().getStackInSlot(0);
        renderItem(pBlockEntity, pPoseStack, pBufferSource, pPackedLight, stack);
        renderInformation(pPoseStack, pBufferSource, pPackedLight, stack);
    }

    private void renderInformation(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedOverlay, ItemStack stack) {
        String repairingItemName = stack.getHoverName().getString();
        if (stack.getDamageValue() > 0) {
            renderText(Component.literal("Repairing: " + repairingItemName), 0xFFFFFF, 0, 0, 0, pPoseStack, pBufferSource);
            renderText(Component.literal("Restored: " + (stack.getMaxDamage() - stack.getDamageValue()) + " / " + stack.getMaxDamage()), 0xFFFFFF, 0, -0.05, 0, pPoseStack, pBufferSource);
            renderFillBar(stack, pPoseStack, pBufferSource, pPackedOverlay);
        } else if (!stack.isEmpty()) {
            renderText(Component.literal("Restored: " + repairingItemName), 0xFFFFFF, 0, -0.03, 0, pPoseStack, pBufferSource);
            renderFillBar(stack, pPoseStack, pBufferSource, pPackedOverlay);
        } else {
            renderText(Component.literal("Insert broken item to repair it"), 0xFFFFFF, 0, -0.03, 0, pPoseStack, pBufferSource);
        }
    }

    private void renderFillBar(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        if (stack.isEmpty()) return;

        float fillRatio = stack.getMaxDamage() > 0
                ? (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage()
                : 1.0f;

        Direction facing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/white_concrete"));

        poseStack.pushPose();

        // Rotate around block center
        poseStack.translate(0.5, 0.365, 0.5);
        float angle = -facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        float p = 1.0f / 16.0f;
        float width = 9.0f * p;
        float height = 0.3f * p;
        float yOffset = -4.0f * p;

        float xMin = -width / 2.0f;
        float xMax = xMin + (width * fillRatio);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f pose = poseStack.last().pose();

        if (fillRatio > 0) {
            // Translate to the front panel position relative to rotated center
            poseStack.translate(0, 0.13D, 0.58);
            //bg
            addQuad(pose, vertexConsumer, xMin, xMin + width, yOffset, yOffset + height, packedOverlay, sprite, 0xff1f50da);

            // Move the bar slightly in front of bg
            poseStack.translate(0, 0, 0.0001D);
            //bar
            addQuad(pose, vertexConsumer, xMin, xMax, yOffset, yOffset + height, packedOverlay, sprite, 0xff1f81da);
        }

        poseStack.popPose();
    }

    private void renderText(Component textToRender, int color, double x, double y, double z, PoseStack poseStack, MultiBufferSource bufferSource) {
        Direction facing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        poseStack.pushPose();

        // Rotate around block center
        poseStack.translate(0.5, 0.365, 0.5);
        float angle = -facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        // Translate to the front panel position relative to rotated center
        poseStack.translate(x, y, 0.58 + z - 0.002);

        float scale = 0.004f;
        poseStack.scale(scale, -scale, scale);

        float width = this.font.width(textToRender);
        float xOffset = -(width / 2.0f);

        this.font.drawInBatch(textToRender, xOffset, 0, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
    }

    private void renderItem(RepairStationBlockEntity pBlockEntity, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, ItemStack stack) {
        if (stack.isEmpty()) return;

        pPoseStack.pushPose();

        // 1. BASE ANCHOR
        pPoseStack.translate(0.5, 0.0, 0.5);

        // 2. FACING ROTATION
        BlockState state = pBlockEntity.getBlockState();
        if (state.hasProperty(RepairStationBlock.FACING)) {
            Direction facing = state.getValue(RepairStationBlock.FACING);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        // 3. POSITION
        // Adjusted height slightly to ensure it doesn't clip with the flip
        boolean isBlockItem = stack.getItem() instanceof BlockItem;
        float height = isBlockItem ? 0.83f : 0.73f;
        float forwardOffset = 0.1f;
        pPoseStack.translate(0.0, height, forwardOffset);

        // 4. APPLY SLOPE (22.5f)
        pPoseStack.mulPose(Axis.XP.rotationDegrees(23f));

        // 5. FLIP & ORIENT
        pPoseStack.translate(0.0, 0.05, 0.0); // Anti-Z-fighting

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
