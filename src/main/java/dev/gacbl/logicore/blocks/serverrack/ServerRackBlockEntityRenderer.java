package dev.gacbl.logicore.blocks.serverrack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gacbl.logicore.blocks.serverrack.client.ServerRackBlockRenderer;
import dev.gacbl.logicore.blocks.serverrack.client.ServerRackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ServerRackBlockEntityRenderer extends ServerRackBlockRenderer {
    private final ItemModelResolver itemModelResolver;

    public ServerRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public void extractRenderState(@NonNull ServerRackBlockEntity blockEntity, @NonNull ServerRackRenderState renderState, float partialTick, @NonNull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay damageOverlayState) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, damageOverlayState);
        renderState.facing = blockEntity.getBlockState().getValue(ServerRackBlock.FACING);
        var handler = blockEntity.getItemHandler();
        for (int i = 0; i < ServerRackBlockEntity.RACK_CAPACITY; i++) {
            ItemStack stack = handler.getResource(i).toStack();
            itemModelResolver.updateForTopItem(renderState.slotItems[i], stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(@NonNull ServerRackRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector renderTasks, @NonNull CameraRenderState cameraRenderState) {
        super.submit(renderState, poseStack, renderTasks, cameraRenderState);

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.1f, 0.5f);

        switch (renderState.facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YN.rotationDegrees(90));
        }

        for (int i = 0; i < ServerRackBlockEntity.RACK_CAPACITY; i++) {
            if (!renderState.slotItems[i].isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0f, 0.7f - i * 0.175f, 0.098f);
                poseStack.scale(0.98f, 0.98f, 0.98f);
                renderState.slotItems[i].submit(poseStack, renderTasks, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }
}
