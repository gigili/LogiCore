package dev.gacbl.logicore.api.compat.jei;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.compiler.recipe.CompilerRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CompilerRecipeCategory implements IRecipeCategory<CompilerRecipe> {
    public static final RecipeType<CompilerRecipe> TYPE = RecipeType.create(LogiCore.MOD_ID, "compiler", CompilerRecipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/gui/compiler_ui.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public CompilerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 8, 15, 166, 88);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CompilerModule.COMPILER_BLOCK.get()));
        this.localizedName = Component.translatable("block.logicore.compiler");
    }

    @Override
    public @NotNull RecipeType<CompilerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return localizedName;
    }

    @SuppressWarnings("removal")
    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CompilerRecipe recipe, @NotNull IFocusGroup focuses) {
        if (Minecraft.getInstance().level == null) return;

        List<ItemStack> inputStacks = Arrays.stream(recipe.inputItem().getItems())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(recipe.inputCount());
                    return copy;
                })
                .toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 5, 6)
                .addItemStacks(inputStacks)
                .setSlotName("Input");

        builder.addSlot(RecipeIngredientRole.OUTPUT, 137, 52)
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()))
                .setSlotName("Output");
    }

    @Override
    public void draw(CompilerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean hasChance = recipe.chance() < 1.0f;

        Component cyclesText = Component.literal(recipe.cycles() + " Cycles");
        guiGraphics.drawString(minecraft.font, cyclesText, 50, hasChance ? 45 : 60, 0x808080, false);

        if (hasChance) {
            String chanceStr = "Chance: " + (int) (recipe.chance() * 100) + "%";
            guiGraphics.drawString(minecraft.font, chanceStr, 50, 60, 0xFF0000, false);
        }
    }
}
