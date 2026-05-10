package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ToolMaterial;

public class CyclePickItem extends Item {
    public CyclePickItem(ToolMaterial material, Properties properties) {
        super(material.applyToolProperties(properties, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, 1.0F, -2.8F, 0.0F));
    }

    public static ShapedRecipeBuilder getRecipe(HolderGetter<Item> items) {
        return ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, new ItemStackTemplate(CyclePickModule.CYCLE_PICK.get(), 1))
                .pattern("DPD")
                .pattern(" S ")
                .pattern(" S ")
                .define('D', ItemTags.create(Identifier.fromNamespaceAndPath("c", "gems/diamond")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())
                .define('S', ItemTags.create(Identifier.fromNamespaceAndPath("c", "rods/wooden")));
    }
}
