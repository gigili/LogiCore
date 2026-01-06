package dev.gacbl.logicore.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
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

    public ResearchToast(ItemStack icon) {
        this.icon = icon;
        this.title = Component.translatable("logicore.toast.research_complete").withStyle(style -> style.withColor(0xFFD700)); // Gold color
        this.description = icon.getHoverName();
    }

    @Override
    public @NotNull Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, 160, 32);
        guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, -1, false);
        guiGraphics.drawString(toastComponent.getMinecraft().font, this.description, 30, 18, -1, false);
        guiGraphics.renderFakeItem(this.icon, 8, 8);
        return timeSinceLastVisible >= DISPLAY_TIME ? Visibility.HIDE : Visibility.SHOW;
    }
}
