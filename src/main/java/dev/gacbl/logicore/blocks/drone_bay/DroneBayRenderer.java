package dev.gacbl.logicore.blocks.drone_bay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class DroneBayRenderer implements BlockEntityRenderer<DroneBayBlockEntity> {
    private final Font font;
    private BlockState blockState;
    private DroneBayBlockEntity blockEntity;
    private MultiBufferSource bufferSource;
    private PoseStack poseStack;

    public DroneBayRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(DroneBayBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        this.blockState = blockEntity.getBlockState();
        this.blockEntity = blockEntity;
        this.bufferSource = bufferSource;
        this.poseStack = poseStack;

        renderText("Cycles: " + Utils.formatValues(blockEntity.getCyclesStored()), 0x00FF00, 0, -0.1, -0.35);
        renderText("Docked: Nothing", 0xb00307, 0, -0.2, -0.35);
        renderText("What ever", 0x00FF00, 0, -0.3, -0.35);


    }

    private void renderText(String textToRender, int color, double x, double y, double z) {
        Direction facing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;


        poseStack.pushPose();

        poseStack.translate(0.5, 1, 0.5);

        float angle = -facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        poseStack.translate(x, y, z);

        float scale = 0.005f;
        poseStack.scale(scale, -scale, scale);

        float width = this.font.width(textToRender);
        float xOffset = -(width / 2.0f);

        this.font.drawInBatch(textToRender, xOffset, 0, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
    }
}
