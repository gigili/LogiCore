package dev.gacbl.logicore.network;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.network.payload.SyncCableDataPayload;
import dev.gacbl.logicore.network.payload.SyncCycleDataPayload;
import dev.gacbl.logicore.network.payload.SyncDataCenterPositionPayload;
import dev.gacbl.logicore.network.payload.SyncMultiblockDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    private static PayloadRegistrar registrar;

    public static void register(final IEventBus modEventBus) {
        modEventBus.addListener(PacketHandler::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        registrar = event.registrar(LogiCore.MOD_ID).versioned("1.0");

        registrar.playToClient(
                SyncCycleDataPayload.TYPE,
                SyncCycleDataPayload.STREAM_CODEC,
                SyncCycleDataPayload.HANDLER
        );

        registrar.playToClient(
                SyncMultiblockDataPayload.TYPE,
                SyncMultiblockDataPayload.STREAM_CODEC,
                SyncMultiblockDataPayload.HANDLER
        );

        registrar.playToClient(
                SyncCableDataPayload.TYPE,
                SyncCableDataPayload.STREAM_CODEC,
                SyncCableDataPayload.HANDLER
        );

        registrar.playToServer(
                SyncDataCenterPositionPayload.TYPE,
                SyncDataCenterPositionPayload.STREAM_CODEC,
                SyncDataCenterPositionPayload.HANDLER
        );
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingChunk(Level level, BlockPos pos, MSG packet) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), packet);
        }
    }
}
