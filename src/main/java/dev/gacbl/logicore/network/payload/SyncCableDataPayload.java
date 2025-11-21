package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;
import java.util.UUID;

public record SyncCableDataPayload(BlockPos pos, Optional<UUID> networkUUID) implements CustomPacketPayload {

    public static final Type<SyncCableDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_cable_data"));

    public static final StreamCodec<ByteBuf, SyncCableDataPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncCableDataPayload::pos,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), SyncCableDataPayload::networkUUID,
            SyncCableDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SyncCableDataPayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity be = level.getBlockEntity(payload.pos());
                if (be instanceof DataCableBlockEntity cable) {
                    cable.setClientNetworkUUID(payload.networkUUID().orElse(null));
                }
            }
        });
    };
}
