package dev.gacbl.logicore.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.network.payload.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = LogiCore.MOD_ID, value = Dist.CLIENT)
public class ClientPayloadHandler {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(LogiCore.MOD_ID).versioned("1.0");

        registrar.playToClient(
                SyncCycleDataPayload.TYPE,
                SyncCycleDataPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    var level = Minecraft.getInstance().level;
                    if (level != null) {
                        BlockEntity be = level.getBlockEntity(payload.pos());
                        if (be instanceof CoreCycleProviderBlockEntity cycleBe) {
                            cycleBe.setClientData(payload.energy(), payload.cycles(), payload.isGenerating());
                        }
                    }
                })
        );

        registrar.playToClient(
                SyncMultiblockDataPayload.TYPE,
                SyncMultiblockDataPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
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
                })
        );

        registrar.playToClient(
                SyncCableDataPayload.TYPE,
                SyncCableDataPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    var level = Minecraft.getInstance().level;
                    if (level != null) {
                        BlockEntity be = level.getBlockEntity(payload.pos());
                        if (be instanceof DataCableBlockEntity cable) {
                            cable.setClientNetworkUUID(payload.networkUUID().orElse(null));
                        }
                    }
                })
        );

        registrar.playToClient(
                SyncPlayerCyclesPayload.TYPE,
                SyncPlayerCyclesPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ClientCycleData.setCycles(payload.cycles())
                )
        );

        registrar.playToClient(
                SyncPlayerKnowledgePayload.TYPE,
                SyncPlayerKnowledgePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ClientKnowledgeData.add(payload.itemKey())
                )
        );

        registrar.playToClient(
                SyncAllPlayerKnowledgePayload.TYPE,
                SyncAllPlayerKnowledgePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ClientKnowledgeData.clear();
                    ClientKnowledgeData.addAll(payload.itemKeys());
                })
        );

        registrar.playToClient(
                NotifyResearchCompletePayload.TYPE,
                NotifyResearchCompletePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        Minecraft.getInstance().getToasts().addToast(new ResearchToast(payload.stack()))
                )
        );
    }
}
