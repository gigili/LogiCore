package dev.gacbl.logicore.blocks.serverrack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gacbl.logicore.blocks.serverrack.client.ServerRackBlockRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ServerRackBlockEntityRenderer extends ServerRackBlockRenderer {
    private final ItemRenderer itemRenderer;

    public ServerRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super();
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(@NotNull ServerRackBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        super.render(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);

        ItemStackHandler handler = pBlockEntity.getItemHandler();
        Direction facing = pBlockEntity.getBlockState().getValue(ServerRackBlock.FACING);

        pPoseStack.pushPose();

        // Center and rotate based on block facing
        pPoseStack.translate(0.5, 0, 0.5);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pPoseStack.translate(-0.5, 0, -0.5);

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                pPoseStack.pushPose();

                double yOffset = 1.8 - (i * 0.185);
                double zOffset = 0.42;

                pPoseStack.translate(0.5, yOffset, zOffset);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180));

                this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 0);

                pPoseStack.popPose();
            }
        }

        pPoseStack.popPose();
    }
}
