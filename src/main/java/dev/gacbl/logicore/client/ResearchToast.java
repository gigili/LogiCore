package dev.gacbl.logicore.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ResearchToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_TIME = 5000L;

    private final ItemStack icon;
    private final Component title;
    private final Component description;

    private Visibility wantedVisibility = Visibility.SHOW;

    public ResearchToast(ItemStack icon) {
        this.icon = icon;
        this.title = Component.translatable("logicore.toast.research_complete").withStyle(style -> style.withColor(0xFFD700)); // Gold color
        this.description = icon.getHoverName();
    }

    @Override
    public @NotNull Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(@NotNull ToastManager toastManager, long l) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, @NotNull Font font, long fullyVisibleFor) {
        if (fullyVisibleFor >= DISPLAY_TIME) {
            this.wantedVisibility = Visibility.HIDE;
        }

        guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, 160, 32);
        guiGraphics.drawString(font, this.title, 30, 7, -1, false);
        guiGraphics.drawString(font, this.description, 30, 18, -1, false);
        guiGraphics.renderFakeItem(this.icon, 8, 8);
    }
}
