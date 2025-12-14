package dev.gacbl.logicore.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.core.IntegrationUtils;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncPlayerCyclesPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

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

            event.getToolTip().add(Component.translatable("tooltip.logicore.items.cycles", value)
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Level level = event.getEntity().level();
        if (level.getServer() == null) return;

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(event.getEntity().getUUID());
        if (player != null) {
            if (level instanceof ServerLevel serverLevel) {
                CycleSavedData data = CycleSavedData.get(serverLevel);
                long cycles = data.getCyclesByKeyString(IntegrationUtils.getStorageKey(serverLevel, player.getUUID()));
                PacketHandler.sendToPlayer(player, new SyncPlayerCyclesPayload(cycles));
            }
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
                        10, 10, 0xFFFFFF // X=10, Y=10, White
                );
            } else {
                gui.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("tooltip.logicore.ui.cloud_cycles", Utils.formatValues(cycles)),
                        10, 10, 0xFFFFFF // X=10, Y=10, White
                );
            }
        });
    }
}
