package dev.gacbl.logicore.data.builder;

import dev.gacbl.logicore.blocks.compiler.recipe.CompilerRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class CompilerRecipeBuilder implements RecipeBuilder {
    private final Ingredient input;
    private final int inputCount;
    private final ItemStack output;
    private final int cycles;
    private final float chance;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final int time;
    @Nullable
    private String group;

    private CompilerRecipeBuilder(Ingredient input, int inputCount, ItemStack output, int cycles, float chance, int time) {
        this.input = input;
        this.inputCount = inputCount;
        this.output = output;
        this.cycles = cycles;
        this.chance = chance;
        this.time = time;
    }

    public static CompilerRecipeBuilder of(Ingredient input, int inputCount, ItemLike output, int outputCount, int cycles, float chance, int time) {
        return new CompilerRecipeBuilder(input, inputCount, new ItemStack(output, outputCount), cycles, chance, time);
    }

    public static CompilerRecipeBuilder of(Ingredient input, ItemLike output, int cycles, int time) {
        return new CompilerRecipeBuilder(input, 1, new ItemStack(output, 1), cycles, 1.0f, 10);
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return output.getItem();
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }

        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancement::addCriterion);

        CompilerRecipe recipe = new CompilerRecipe(
                this.input,
                this.inputCount,
                this.output,
                this.cycles,
                this.chance,
                this.time
        );

        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipes/compiler/")));
    }
}
