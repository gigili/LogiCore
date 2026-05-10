package dev.gacbl.logicore.items.processorunit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ProcessorUnitItem extends Item {
    public final ProcessorUnitTier tier;

    public ProcessorUnitItem(Item.Properties properties, ProcessorUnitTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        Style tierStyle = Style.EMPTY.withColor(ChatFormatting.getByCode(tier.tooltipPrefix.charAt(1)));
        tooltipComponents.accept(Component.translatable("item.processorunit.cycle_generation_tooltip", tier.cycleRate.get()).withStyle(tierStyle));
    }
}
