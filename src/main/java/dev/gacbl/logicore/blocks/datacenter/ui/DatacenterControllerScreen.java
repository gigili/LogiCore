package dev.gacbl.logicore.blocks.datacenter.ui;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class DatacenterControllerScreen extends MyAbstractContainerScreen<DatacenterControllerMenu> {
    public DatacenterControllerScreen(DatacenterControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 60;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/datacenter_controller_ui.png");
        renderInventoryLabel = false;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (Config.RENDER_MACHINE_INFORMATION_IN_UI.get()) {
            /*Component text;
            if (menu.getIsFormed()) {
                text = Component.translatable("message.logicore.datacenter.formed");
            } else if (menu.getLastException() != null) {
                String errorPos = menu.getLastException().pos != null ? menu.getLastException().pos.toShortString() : "";
                text = Component.translatable(menu.getLastException().message, errorPos);
            } else {
                text = Component.translatable("errors.logicore.datacenter.invalid_form");
            }
            graphics.drawString(this.font, text, leftPos + 65, topPos + 120, this.titleLabelColor);*/
        }
    }
}
