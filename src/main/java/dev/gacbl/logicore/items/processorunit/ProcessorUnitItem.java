package dev.gacbl.logicore.items.processorunit;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ProcessorUnitItem extends Item {
    public ProcessorUnitItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ProcessorUnitModule.PROCESSOR_UNIT.get())
                .pattern("RQR")
                .pattern("IGI")
                .pattern("RQR")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE)
                .define('Q', Items.QUARTZ);
    }
}
