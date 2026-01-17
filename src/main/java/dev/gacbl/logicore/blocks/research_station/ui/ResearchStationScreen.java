package dev.gacbl.logicore.blocks.research_station.ui;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ResearchStationScreen extends MyAbstractContainerScreen<ResearchStationMenu> {
    public ResearchStationScreen(ResearchStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 65;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/research_station_ui.png");
        renderInventoryLabel = false;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (Config.RENDER_MACHINE_INFORMATION_IN_UI.get()) {
            float fillRatio = (float) menu.getProgress() / (float) menu.getMaxProgress();
            int percentage = (int) (fillRatio * 100);

            Component textTime = Component.translatable("tooltip.logicore.research_time", menu.getMaxProgress() / 20);
            graphics.drawString(this.font, textTime, leftPos + 65, topPos + 110, this.titleLabelColor);

            Component text = Component.translatable("tooltip.logicore.research_progress", percentage);
            graphics.drawString(this.font, text, leftPos + 65, topPos + 123, this.titleLabelColor);
        }
    }
}
