package dev.gacbl.logicore.core.ui;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class MyAbstractContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static ResourceLocation TEXTURE;
    protected boolean renderInventoryLabel = true;
    protected boolean renderTitleLabel = true;
    protected int titleLabelColor = 10461087;
    protected int inventoryLabelColor = 7303023;

    public MyAbstractContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 231;
        this.imageHeight = 243;
    }

    public static ResourceLocation getTexture() {
        return TEXTURE;
    }

    public static void setTexture(String texture) {
        MyAbstractContainerScreen.TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, texture);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        if (renderTitleLabel) {
            graphics.drawString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), this.titleLabelX, this.titleLabelY, titleLabelColor, true);
        }

        if (renderInventoryLabel) {
            graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, inventoryLabelColor, false);
        }
    }
}
