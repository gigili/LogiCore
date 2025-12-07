package dev.gacbl.logicore.data;

import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlock;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlock;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlock;
import dev.gacbl.logicore.blocks.generator.GeneratorBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.entity.drone.DroneItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ServerRackBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        DataCableBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        ComputerBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        ProcessorUnitItem.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        DatacenterControllerBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        CompilerBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        DatacenterPortBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        DroneBayBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        DroneItem.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        GeneratorBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);

        /*CompilerRecipeBuilder.of(
                        Ingredient.of(Items.IRON_INGOT), // Input
                        3,                               // Input Count
                        Items.GOLD_INGOT,                // Output
                        1,                               // Output Count
                        200,                             // Cycles
                        1.0f,                            // Chance
                        30                               // Ticks
                )
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "gold_from_iron_compiler"));

        CompilerRecipeBuilder.of(
                        Ingredient.of(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "stones"))), // Input
                        16,                               // Input Count
                        Items.IRON_BLOCK,                // Output
                        1,                               // Output Count
                        500,                             // Cycles
                        0.65f,                            // Chance
                        50                               // Ticks
                )
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "iron_block_from_stone_compiler"));*/
    }
}
