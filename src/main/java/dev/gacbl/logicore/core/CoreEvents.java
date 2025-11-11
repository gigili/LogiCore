package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.network.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = LogiCore.MOD_ID)
public class CoreEvents {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            NetworkManager.get(serverLevel).tick(serverLevel);
        }
    }
}
