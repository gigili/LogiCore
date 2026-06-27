package dev.gacbl.logicore.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.List;

import static dev.gacbl.logicore.LogiCore.*;

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
            boolean isResearched = ClientKnowledgeData.isUnlocked(Utils.getItemKey(event.getItemStack()));
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
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientKnowledgeData.clear();
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getCrafting().is(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())) {
            if (List.of(LASH_UUID, DIRE_UUID, GAC_UUID, DEV_UUID).contains(player.getUUID())) {
                grantDevAdvancement(player);
            }
        }
    }

    private static void grantDevAdvancement(ServerPlayer player) {
        String username = player.getName().getString().toLowerCase();
        ResourceLocation advId = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, username);
        AdvancementHolder advancement = player.server.getAdvancements().get(advId);

        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);

            if (!progress.isDone()) {
                player.getAdvancements().award(advancement, "is_" + username);
            }
        }
    }
}
