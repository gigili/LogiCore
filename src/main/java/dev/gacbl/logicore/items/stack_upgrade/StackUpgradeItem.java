package dev.gacbl.logicore.items.stack_upgrade;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StackUpgradeItem extends Item {
    public StackUpgradeItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, StackUpgradeModule.STACK_UPGRADE.get())
                .pattern("EEE")
                .pattern("EPE")
                .pattern("EEE")
                .define('E', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/emerald")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.item.logicore.stack_upgrade"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
