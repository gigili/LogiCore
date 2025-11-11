package dev.gacbl.logicore.data;

import dev.gacbl.logicore.cpucore.CPUCoreBlock;
import dev.gacbl.logicore.serverrack.ServerRackBlock;
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
        CPUCoreBlock.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
    }
}
