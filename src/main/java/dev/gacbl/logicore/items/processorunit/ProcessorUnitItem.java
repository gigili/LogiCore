package dev.gacbl.logicore.items.processorunit;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ProcessorUnitItem extends Item {
    public ProcessorUnitItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ProcessorUnitModule.PROCESSOR_UNIT.get())
                .pattern("RQR")
                .pattern("QEQ")
                .pattern("RQR")
                .define('E', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ender_pearls")))
                .define('R', Items.REDSTONE)
                .define('Q', Items.QUARTZ);
    }
}
