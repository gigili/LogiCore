package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class CyclePickItem extends PickaxeItem {
    public CyclePickItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, CyclePickModule.CYCLE_PICK.get())
                .pattern("DPD")
                .pattern(" S ")
                .pattern(" S ")
                .define('D', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/diamond")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get())
                .define('S', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden")));
    }
}
