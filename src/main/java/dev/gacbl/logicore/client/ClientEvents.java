package dev.gacbl.logicore.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = LogiCore.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        CycleValueManager.reload(
                event.getRecipeManager(),
                net.minecraft.client.Minecraft.getInstance().level.registryAccess(),
                null,
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
}
