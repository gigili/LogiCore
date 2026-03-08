package dev.gacbl.logicore.items.server.ui;

import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.server.ServerModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

public class ServerMenu extends MyAbstractContainerMenu {
    private final ItemStack serverStack;
    private final SimpleContainer container;

    public ServerMenu(int containerId, Inventory playerInventory, ItemStack stack) {
        super(ServerModule.SERVER_MENU.get(), containerId, playerInventory, null, new SimpleContainerData(2));
        this.serverStack = stack;
        this.TE_INVENTORY_SLOT_COUNT = 9;

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        this.container = new SimpleContainer(TE_INVENTORY_SLOT_COUNT);
        contents.copyInto(container.getItems());

        for (int i = 0; i < TE_INVENTORY_SLOT_COUNT; i++) {
            this.addSlot(new Slot(container, i, 36 + i * 18, 89) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    saveToItem();
                }

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.getItem() instanceof ProcessorUnitItem;
                }

                @Override
                public int getMaxStackSize(@NotNull ItemStack stack) {
                    return 1;
                }
            });
        }
    }

    private void saveToItem() {
        serverStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == serverStack;
    }
}
