package dev.gacbl.logicore.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncPlayerKnowledgePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;

@EventBusSubscriber(modid = LogiCore.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        assert net.minecraft.client.Minecraft.getInstance().level != null;
        CycleValueManager.reload(
                event.getRecipeManager(),
                net.minecraft.client.Minecraft.getInstance().level.registryAccess(),
                false
        );
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (CycleValueManager.hasCycleValue(event.getItemStack())) {
            int value = CycleValueManager.getCycleValue(event.getItemStack());
            event.getToolTip().add(Component.translatable("tooltip.logicore.items.cycles", value).withStyle(ChatFormatting.AQUA));
            boolean isResearched = ClientKnowledgeData.isUnlocked(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString());
            event.getToolTip().add(Component.translatable(isResearched ? "tooltip.logicore.items.researched" : "tooltip.logicore.items.not_researched").withStyle(isResearched ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_hud"), (gui, deltaTracker) -> {
            long cycles = ClientCycleData.getCycles();

            Player player = Minecraft.getInstance().player;
            if (player != null && player.isShiftKeyDown()) {
                gui.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("tooltip.logicore.ui.cloud_cycles", cycles),
                        10, 10, 0xFFFFFF
                );
            } else {
                gui.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("tooltip.logicore.ui.cloud_cycles", Utils.formatValues(cycles)),
                        10, 10, 0xFFFFFF
                );
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            CycleSavedData data = CycleSavedData.get(level);
            String playerKey = CycleSavedData.getKey(level, player.getUUID());

            Set<String> unlockedItems = data.getKnowledge(playerKey); // You might need to add a getter for this in CycleSavedData

            // 2. Send a packet for EACH item (Simple approach)
            // Optimization Note: In the future, you might want to make a "BulkSyncPayload" to send all at once,
            // but for now, sending individual packets is fine unless they have thousands of items.
            if (unlockedItems != null) {
                for (String itemKey : unlockedItems) {
                    PacketHandler.sendToPlayer(player, new SyncPlayerKnowledgePayload(itemKey));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientKnowledgeData.clear();
    }
}
