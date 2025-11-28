package dev.gacbl.logicore.blocks.compiler.ui;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompilerScreen extends AbstractContainerScreen<CompilerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/gui/compiler_ui.png");

    public CompilerScreen(CompilerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 175;
        this.imageHeight = 188;

        this.titleLabelX = leftPos + 7;
        this.titleLabelY = topPos + 5;

        this.inventoryLabelX = leftPos + 7;
        this.inventoryLabelY = topPos + 96;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, 175, 188);
        renderTooltip(graphics, mouseX, mouseY);

        if (menu.isCrafting()) {
            graphics.blit(TEXTURE, x + 63, y + 36, 176, 0, menu.getScaledArrowProgress(), 29);
        }
    }
}
