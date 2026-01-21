package dev.gacbl.logicore.blocks.serverrack.ui;

import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ServerRackMenu extends MyAbstractContainerMenu {
    public ServerRackMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(10));
    }

    public ServerRackMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ServerRackModule.SERVER_RACK_MENU.get(), containerId, playerInventory, entity, data);
        this.TE_INVENTORY_SLOT_COUNT = 9;
        int slotSize = 18;
        int startX = 36;
        int startY = 133;

        for (int row = 0; row < 9; row++) {
            this.addSlot(new SlotItemHandler(((CoreCycleProviderBlockEntity) this.blockEntity).getItemHandler(), row, startX + row * slotSize, startY) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(ProcessorUnitModule.PROCESSOR_UNIT.get());
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
    }

    public ServerRackMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 0-35 is Player Inventory
            // 36-44 is Server Rack Inventory
            int rackStartIndex = 36;
            int rackEndIndex = 36 + TE_INVENTORY_SLOT_COUNT; // 45

            if (index < rackStartIndex) {
                // We are in Player Inventory (0-35)
                // Try to move ProcessorUnits to the Rack
                if (slotStack.is(ProcessorUnitModule.PROCESSOR_UNIT.get())) {
                    if (!this.moveItemStackTo(slotStack, rackStartIndex, rackEndIndex, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 27) { // 0-26 is Hotbar + partial main?
                    // Hotbar is 0-8, Main Inv is 9-35
                    if (index < 9) { // Hotbar -> Main Inv
                        if (!this.moveItemStackTo(slotStack, 9, 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else { // Main Inv -> Hotbar
                        if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else {
                // We are in the Rack (36-44) -> Move to Player Inventory
                if (!this.moveItemStackTo(slotStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    public int getCycles() {
        return this.data.get(2);
    }

    public int getMaxCycles() {
        return this.data.get(3);
    }

    public int getBaseCycleGeneration() {
        return this.data.get(4);
    }

    public int getCyclesPerProcessor() {
        return this.data.get(5);
    }

    public int getFePerCycle() {
        return this.data.get(6);
    }

    public int getProcessorCount() {
        return this.data.get(7);
    }

    public boolean hasDataCenterBoost() {
        return this.data.get(8) == 1;
    }

    public int getDataCenterBoost() {
        return this.data.get(9);
    }
}
