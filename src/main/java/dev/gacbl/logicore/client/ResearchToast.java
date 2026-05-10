package dev.gacbl.logicore.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ResearchToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_TIME = 5000L;

    private final ItemStack icon;
    private final Component title;
    private final Component description;
    private long visibleTime = 0;

    public ResearchToast(ItemStack icon) {
        this.icon = icon;
        this.title = Component.translatable("logicore.toast.research_complete").withStyle(style -> style.withColor(0xFFD700));
        this.description = icon.getHoverName();
    }

    @Override
    public void update(ToastManager manager, long timeSinceLastVisible) {
        this.visibleTime = timeSinceLastVisible;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibleTime < DISPLAY_TIME ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, Font font, long timeSinceLastVisible) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, 160, 32);
        guiGraphics.text(font, this.title, 30, 7, -1, false);
        guiGraphics.text(font, this.description, 30, 18, -1, false);
        guiGraphics.fakeItem(this.icon, 8, 8);
    }
}
