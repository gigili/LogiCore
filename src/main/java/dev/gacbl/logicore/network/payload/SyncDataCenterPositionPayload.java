package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record SyncDataCenterPositionPayload(BlockPos controllerPos, BlockPos entityPos) implements CustomPacketPayload {

    public static final Type<SyncDataCenterPositionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_cycle_data"));

    public static final StreamCodec<ByteBuf, SyncDataCenterPositionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncDataCenterPositionPayload::controllerPos,
            BlockPos.STREAM_CODEC, SyncDataCenterPositionPayload::entityPos,
            SyncDataCenterPositionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SyncDataCenterPositionPayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null && !level.isClientSide) {
                BlockEntity be = level.getBlockEntity(payload.entityPos());
                if (be instanceof CoreCycleProviderBlockEntity cycleBe) {
                    if (cycleBe.dataCenterController != payload.controllerPos()) {
                        cycleBe.setDataCenterController(payload.controllerPos());
                    }
                }
            }
        });
    };
}
