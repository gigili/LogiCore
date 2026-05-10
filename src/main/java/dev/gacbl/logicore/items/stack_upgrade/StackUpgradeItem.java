package dev.gacbl.logicore.items.stack_upgrade;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class StackUpgradeItem extends Item {
    public StackUpgradeItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe(HolderGetter<Item> items) {
        return ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, StackUpgradeModule.STACK_UPGRADE.get())
                .pattern("EEE")
                .pattern("EPE")
                .pattern("EEE")
                .define('E', ItemTags.create(Identifier.fromNamespaceAndPath("c", "gems/emerald")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.item.logicore.stack_upgrade"));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
