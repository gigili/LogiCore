package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public record SyncMultiblockPortsPayload(BlockPos controllerPos, boolean formed, Set<BlockPos> ports)
        implements CustomPacketPayload {

    public static final Type<SyncMultiblockPortsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_multiblock_ports"));

    public static final StreamCodec<FriendlyByteBuf, SyncMultiblockPortsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SyncMultiblockPortsPayload::controllerPos,
                    ByteBufCodecs.BOOL,
                    SyncMultiblockPortsPayload::formed,
                    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(LinkedHashSet::new)),
                    SyncMultiblockPortsPayload::ports,
                    SyncMultiblockPortsPayload::new
            );

    public static final IPayloadHandler<SyncMultiblockPortsPayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ServerLevel level = serverPlayer.serverLevel();
            BlockEntity blockEntity = level.getBlockEntity(payload.controllerPos());

            if (blockEntity instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
                controllerBlockEntity.applyMultiblockState(payload.formed(), payload.ports());
            }
        });
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
