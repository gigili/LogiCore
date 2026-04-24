package dev.gacbl.logicore.items.processorunit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProcessorUnitItem extends Item {
    public final ProcessorUnitTier tier;

    public ProcessorUnitItem(ProcessorUnitTier tier) {
        super(new Item.Properties());
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        Style tierStyle = Style.EMPTY.withColor(ChatFormatting.getByCode(tier.tooltipPrefix.charAt(1)));
        tooltipComponents.add(Component.translatable("item.processorunit.cycle_generation_tooltip", tier.cycleRate.get()).withStyle(tierStyle));
    }
}
