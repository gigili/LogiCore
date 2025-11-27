package dev.gacbl.logicore.blocks.compiler.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class CompilerRecipeSerializer implements RecipeSerializer<CompilerRecipe> {
    public static final CompilerRecipeSerializer INSTANCE = new CompilerRecipeSerializer();

    @Override
    public @NotNull MapCodec<CompilerRecipe> codec() {
        return CompilerRecipe.CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CompilerRecipe> streamCodec() {
        return CompilerRecipe.STREAM_CODEC;
    }
}
