package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

public class CyclePickItem extends PickaxeItem {
    public CyclePickItem(Properties properties) {
        super(ModMaterials.CYCLE_PICK, 1f, -2.8f, properties);
    }

    public static ShapedRecipeBuilder getRecipe(HolderGetter<Item> items) {
        return ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, CyclePickModule.CYCLE_PICK.get())
                .pattern("DPD")
                .pattern(" S ")
                .pattern(" S ")
                .define('D', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/diamond")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get())
                .define('S', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden")));
    }
}
