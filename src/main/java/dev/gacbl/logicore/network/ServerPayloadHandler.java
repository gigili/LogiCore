package dev.gacbl.logicore.network;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.network.payload.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = LogiCore.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerPayloadHandler {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(LogiCore.MOD_ID).versioned("1.0");

        registrar.playToClient(
                SyncCycleDataPayload.TYPE,
                SyncCycleDataPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );

        registrar.playToClient(
                SyncMultiblockDataPayload.TYPE,
                SyncMultiblockDataPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );

        registrar.playToClient(
                SyncCableDataPayload.TYPE,
                SyncCableDataPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );

        registrar.playToClient(
                SyncPlayerCyclesPayload.TYPE,
                SyncPlayerCyclesPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );

        registrar.playToClient(
                SyncPlayerKnowledgePayload.TYPE,
                SyncPlayerKnowledgePayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );

        registrar.playToClient(
                SyncAllPlayerKnowledgePayload.TYPE,
                SyncAllPlayerKnowledgePayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );

        registrar.playToClient(
                NotifyResearchCompletePayload.TYPE,
                NotifyResearchCompletePayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
    }
}
