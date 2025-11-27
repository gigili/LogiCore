package dev.gacbl.logicore.blocks.compiler.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public record CompilerRecipe(Ingredient inputItem, int inputCount, ItemStack output, int cycles,
                             float chance) implements Recipe<CompilerRecipeInput> {

    @Override
    public boolean matches(@NotNull CompilerRecipeInput container, @NotNull Level level) {
        if (container.input().isEmpty()) return false;
        return inputItem.test(container.input()) && container.input().getCount() >= inputCount;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CompilerRecipeInput container, HolderLookup.@NotNull Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return output;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CompilerModule.COMPILER_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return CompilerModule.COMPILER_TYPE.get();
    }

    private static final Codec<Pair<Ingredient, Integer>> INPUT_CODEC = new MapCodec<Pair<Ingredient, Integer>>() {
        @Override
        public <T> RecordBuilder<T> encode(Pair<Ingredient, Integer> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }


        @Override
        public <T> DataResult<Pair<Ingredient, Integer>> decode(com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> map) {
            DataResult<Ingredient> ingredientResult = Ingredient.CODEC.decode(ops, ops.createMap(map.entries())).map(Pair::getFirst);

            T countObj = map.get("count");
            int count = (countObj != null) ? ops.getNumberValue(countObj).result().orElse(1).intValue() : 1;

            return ingredientResult.map(ing -> Pair.of(ing, count));
        }
    }.codec();

    private static final Codec<Pair<ItemStack, Float>> OUTPUT_CODEC = new MapCodec<Pair<ItemStack, Float>>() {
        @Override
        public <T> RecordBuilder<T> encode(Pair<ItemStack, Float> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<Pair<ItemStack, Float>> decode(com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> map) {
            DataResult<ItemStack> stackResult = ItemStack.CODEC.decode(ops, ops.createMap(map.entries())).map(Pair::getFirst);

            T chanceObj = map.get("chance");
            float chance = (chanceObj != null) ? ops.getNumberValue(chanceObj).result().orElse(1.0f).floatValue() : 1.0f;

            return stackResult.map(stack -> Pair.of(stack, chance));
        }
    }.codec();

    public static final MapCodec<CompilerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            INPUT_CODEC.fieldOf("input").forGetter(r -> Pair.of(r.inputItem, r.inputCount)),
            OUTPUT_CODEC.fieldOf("output").forGetter(r -> Pair.of(r.output, r.chance)),
            Codec.INT.fieldOf("cycles").forGetter(CompilerRecipe::cycles)
    ).apply(instance, (inputPair, outputPair, cycles) -> new CompilerRecipe(
            inputPair.getFirst(), inputPair.getSecond(),
            outputPair.getFirst(), cycles, outputPair.getSecond()
    )));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompilerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CompilerRecipe::inputItem,
            ByteBufCodecs.INT, CompilerRecipe::inputCount,
            ItemStack.STREAM_CODEC, CompilerRecipe::output,
            ByteBufCodecs.INT, CompilerRecipe::cycles,
            ByteBufCodecs.FLOAT, CompilerRecipe::chance,
            CompilerRecipe::new
    );
}
