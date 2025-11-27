package dev.gacbl.logicore.api.compat.jei;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class LogiCoreJeiPlugin implements IModPlugin {
    public static IDrawableStatic slotDrawable;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(CompilerModule.COMPILER_BLOCK.get()), CompilerRecipeCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CompilerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        slotDrawable = registration.getJeiHelpers().getGuiHelper().getSlotDrawable();
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        final var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(CompilerRecipeCategory.TYPE, recipeManager.getAllRecipesFor(CompilerModule.COMPILER_TYPE.get()).stream().map(RecipeHolder::value).toList());

    }
}
