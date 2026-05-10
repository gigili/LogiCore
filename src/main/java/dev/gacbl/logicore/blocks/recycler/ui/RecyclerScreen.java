package dev.gacbl.logicore.blocks.recycler.ui;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class RecyclerScreen extends MyAbstractContainerScreen<RecyclerMenu> {
    public RecyclerScreen(RecyclerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 96;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/recycler_ui.png");
        renderInventoryLabel = false;
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (Config.RENDER_MACHINE_INFORMATION_IN_UI.get()) {
            float fillRatio = (float) menu.getProgress() / (float) menu.getMaxProgress();
            int percentage = (int) (fillRatio * 100);

            Component text = Component.translatable("tooltip.logicore.recycle_progress", percentage);
            graphics.text(this.font, text, leftPos + 65, topPos + 120, this.titleLabelColor);
        }
    }
}
