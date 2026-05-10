package dev.gacbl.logicore.core.ui;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MyAbstractContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static Identifier TEXTURE;
    protected boolean renderInventoryLabel = true;
    protected boolean renderTitleLabel = true;
    protected int titleLabelColor = 0xFF9F9F9F;
    protected int inventoryLabelColor = 0xFF6F6F6F;

    public MyAbstractContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 231, 243);
    }

    public static Identifier getTexture() {
        return TEXTURE;
    }

    public static void setTexture(String texture) {
        MyAbstractContainerScreen.TEXTURE = Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, texture);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, imageWidth, imageHeight, 256, 256);
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Component screenTitle = this.title;
        if (screenTitle.getString().isBlank()
                && this.menu instanceof MyAbstractContainerMenu modMenu
                && modMenu.getBlockEntity() instanceof MenuProvider menuProvider) {
            screenTitle = menuProvider.getDisplayName();
        }

        if (renderTitleLabel) {
            graphics.text(this.font, screenTitle.copy(), this.titleLabelX, this.titleLabelY, titleLabelColor, true);
        }

        if (renderInventoryLabel) {
            graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, inventoryLabelColor, false);
        }
    }
}
