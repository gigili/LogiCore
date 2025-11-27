package dev.gacbl.logicore.blocks.compiler.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record CompilerRecipeInput(ItemStack input) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int index) {
        return index == 0 ? input : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
