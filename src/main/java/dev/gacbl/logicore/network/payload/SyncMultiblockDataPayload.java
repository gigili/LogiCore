package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record SyncMultiblockDataPayload(
        BlockPos pos,
        boolean isFormed,
        BlockPos minPos,
        BlockPos maxPos,
        String errorMessage,
        BlockPos errorPos
) implements CustomPacketPayload {

    public static final Type<SyncMultiblockDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_multiblock_data"));

    public static final StreamCodec<ByteBuf, SyncMultiblockDataPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncMultiblockDataPayload::pos,
            ByteBufCodecs.BOOL, SyncMultiblockDataPayload::isFormed,
            BlockPos.STREAM_CODEC, SyncMultiblockDataPayload::minPos,
            BlockPos.STREAM_CODEC, SyncMultiblockDataPayload::maxPos,
            ByteBufCodecs.STRING_UTF8, SyncMultiblockDataPayload::errorMessage,
            BlockPos.STREAM_CODEC, SyncMultiblockDataPayload::errorPos,
            SyncMultiblockDataPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SyncMultiblockDataPayload> HANDLER = (payload, context) -> context.enqueueWork(() -> {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity be = level.getBlockEntity(payload.pos());
            if (be instanceof AbstractSealedController controller) {
                controller.setClientData(
                        payload.isFormed(),
                        payload.minPos(),
                        payload.maxPos(),
                        payload.errorMessage(),
                        payload.errorPos()
                );
            }

            if (level.getBlockEntity(payload.pos()) instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
                for (BlockPos pos : controllerBlockEntity.getPorts()) {
                    if (level.getBlockEntity(pos) instanceof DatacenterPortBlockEntity portBlockEntity) {
                        portBlockEntity.setControllerPos(payload.pos());
                    }
                }
            }
        }
    });
}
