package dev.gacbl.logicore.items.server.ui;

import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ServerScreen extends MyAbstractContainerScreen<ServerMenu> {
    public ServerScreen(ServerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 90;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/server_ui.png");
        renderInventoryLabel = false;
    }
}
