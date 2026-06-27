package dev.gacbl.logicore.network;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.network.payload.SetAutoCraftingTemplatePayload;
import dev.gacbl.logicore.network.payload.SyncDataCenterPositionPayload;
import dev.gacbl.logicore.network.payload.SyncMultiblockPortsPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

    public static void register(final IEventBus modEventBus) {
        modEventBus.addListener(PacketHandler::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(LogiCore.MOD_ID).versioned("1.0");

        registrar.playToServer(
                SyncDataCenterPositionPayload.TYPE,
                SyncDataCenterPositionPayload.STREAM_CODEC,
                SyncDataCenterPositionPayload.HANDLER
        );

        registrar.playToServer(
                SetAutoCraftingTemplatePayload.TYPE,
                SetAutoCraftingTemplatePayload.STREAM_CODEC,
                SetAutoCraftingTemplatePayload.HANDLER
        );

        registrar.playToServer(
                SyncMultiblockPortsPayload.TYPE,
                SyncMultiblockPortsPayload.STREAM_CODEC,
                SyncMultiblockPortsPayload.HANDLER
        );
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingChunk(Level level, BlockPos pos, MSG packet) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), packet);
        }
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, MSG packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}
