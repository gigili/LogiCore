package dev.gacbl.logicore.client.ui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KnowledgeMenu extends AbstractContainerMenu {
    public KnowledgeMenu(int containerId, Inventory playerInventory) {
        super(KnowledgeModule.KNOWLEDGE_MENU.get(), containerId);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
