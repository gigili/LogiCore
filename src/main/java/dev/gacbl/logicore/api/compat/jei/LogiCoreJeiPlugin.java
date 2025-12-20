package dev.gacbl.logicore.api.compat.jei;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerScreen;
import dev.gacbl.logicore.network.payload.SetAutoCraftingTemplatePayload;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class LogiCoreJeiPlugin implements IModPlugin {
    public static IDrawableStatic slotDrawable;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        //registration.addRecipeCatalyst(new ItemStack(CompilerModule.COMPILER_BLOCK.get()), CompilerRecipeCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        //registration.addRecipeCategories(new CompilerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        slotDrawable = registration.getJeiHelpers().getGuiHelper().getSlotDrawable();
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        final var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        //registration.addRecipes(CompilerRecipeCategory.TYPE, recipeManager.getAllRecipesFor(CompilerModule.COMPILER_TYPE.get()).stream().map(RecipeHolder::value).toList());
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        if (!Config.ALLOW_JEI_DRAG.get()) return;
        registration.addGhostIngredientHandler(CompilerScreen.class, new IGhostIngredientHandler<>() {
            @Override
            public <I> @NotNull List<Target<I>> getTargetsTyped(@NotNull CompilerScreen gui, @NotNull ITypedIngredient<I> ingredient, boolean doStart) {
                List<Target<I>> targets = new ArrayList<>();

                if (ingredient.getIngredient() instanceof ItemStack) {
                    int x = gui.getGuiLeft() + 72;
                    int y = gui.getGuiTop() + 82;

                    targets.add(new Target<I>() {
                        @Override
                        public @NotNull Rect2i getArea() {
                            return new Rect2i(x, y, 16, 16);
                        }

                        @Override
                        public void accept(@NotNull I ingredient) {
                            ItemStack stack = (ItemStack) ingredient;
                            PacketDistributor.sendToServer(new SetAutoCraftingTemplatePayload(
                                    gui.getMenu().blockEntity.getBlockPos(),
                                    stack
                            ));
                        }
                    });
                }
                return targets;
            }

            @Override
            public void onComplete() {
            }
        });
    }
}
