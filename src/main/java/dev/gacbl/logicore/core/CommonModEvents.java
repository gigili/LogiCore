package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncPlayerCyclesPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LogiCore.MOD_ID)
public class CommonModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        syncCycleData(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncCycleData(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncCycleData(event.getEntity());
    }

    private static void syncCycleData(net.minecraft.world.entity.player.Player playerEntity) {
        if (playerEntity instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            CycleSavedData data = CycleSavedData.get(level);

            String key = IntegrationUtils.getStorageKey(level, serverPlayer.getUUID());
            long cycles = data.getCyclesByKeyString(key);

            PacketHandler.sendToPlayer(serverPlayer, new SyncPlayerCyclesPayload(cycles));
        }
    }
}
