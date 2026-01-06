package dev.gacbl.logicore.blocks.research_station.ui;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ResearchStationScreen extends AbstractContainerScreen<ResearchStationMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/gui/research_station_ui.png");

    public ResearchStationScreen(ResearchStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 231;
        this.imageHeight = 243;

        this.titleLabelX = leftPos + 65;
        this.titleLabelY = topPos + 14;

        this.inventoryLabelX = leftPos + 30;
        this.inventoryLabelY = topPos + 140;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, 231, 243);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), this.titleLabelX, this.titleLabelY, 10461087, true);
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 7303023, false);
    }
}
