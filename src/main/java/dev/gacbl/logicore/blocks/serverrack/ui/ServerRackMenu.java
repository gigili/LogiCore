package dev.gacbl.logicore.blocks.serverrack.ui;

import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ServerRackMenu extends AbstractContainerMenu {
    private final CoreCycleProviderBlockEntity blockEntity;

    public ServerRackMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos));
    }

    public ServerRackMenu(int containerId, Inventory playerInventory, BlockEntity entity) {
        super(ServerRackModule.SERVER_RACK_MENU.get(), containerId);
        this.blockEntity = (CoreCycleProviderBlockEntity) entity;

        int rackSlots = ServerRackBlockEntity.RACK_CAPACITY;
        int slotSize = 18;
        int inventoryX = 8;
        int inventoryY = 58;

        for (int i = 0; i < rackSlots; i++) {
            int x = inventoryX + (i % ServerRackBlockEntity.RACK_CAPACITY) * slotSize;
            int y = 18;
            this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), i, x, y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(ProcessorUnitModule.PROCESSOR_UNIT.get());
                }
            });
        }

        // Add Player Inventory Slots
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, inventoryX + j * slotSize, inventoryY + i * slotSize));
            }
        }

        // Add Player Hotbar Slots
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, inventoryX + i * slotSize, inventoryY + 58));
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

            int playerInvStart = ServerRackBlockEntity.RACK_CAPACITY;
            int playerInvEnd = playerInvStart + 27; // 3 rows
            int playerHotbarEnd = playerInvEnd + 9;

            if (index < playerInvStart) {
                // Trying to move item from rack to player inventory
                if (!this.moveItemStackTo(slotStack, playerInvStart, playerHotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else {
                // Trying to move item from player inventory to rack
                if (slotStack.is(ProcessorUnitModule.PROCESSOR_UNIT.get())) {
                    if (!this.moveItemStackTo(slotStack, 0, playerInvStart, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < playerInvEnd) {
                    // Move from the main inventory to the hotbar
                    if (!this.moveItemStackTo(slotStack, playerInvEnd, playerHotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < playerHotbarEnd) {
                    // Move from the hotbar to the main inventory
                    if (!this.moveItemStackTo(slotStack, playerInvStart, playerInvEnd, false)) {
                        return ItemStack.EMPTY;
                    }
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        Level level = player.level();
        return level.getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity &&
                player.distanceToSqr(this.blockEntity.getBlockPos().getCenter()) <= 64.0;
    }
}
