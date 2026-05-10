package dev.gacbl.logicore.blocks.compiler.ui;

import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class CompilerMenu extends MyAbstractContainerMenu {
    public static final int TEMPLATE_SLOT_INDEX = 0;
    public CompilerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(3));
    }

    public CompilerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(CompilerModule.COMPILER_MENU.get(), containerId, playerInventory, entity, data);
        this.TE_INVENTORY_SLOT_COUNT = 3;

        var handler = ((CompilerBlockEntity) this.blockEntity).getInternalItemHandler();

        this.addSlot(new ResourceHandlerSlot(handler, handler::set, CompilerBlockEntity.INPUT_SLOT, 72, 82) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new ResourceHandlerSlot(handler, handler::set, CompilerBlockEntity.OUTPUT_SLOT, 144, 82) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        var upgradeHandler = ((CompilerBlockEntity) this.blockEntity).getUpgradeItemHandler(null);
        this.addSlot(new ResourceHandlerSlot(upgradeHandler, upgradeHandler::set, 0, 108, 118));
    }

    public CompilerMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        ItemStack copyOfSourceStack = ItemStack.EMPTY;
        Slot sourceSlot = slots.get(pIndex);

        if (sourceSlot != null && sourceSlot.hasItem()) {
            ItemStack sourceStack = sourceSlot.getItem();
            copyOfSourceStack = sourceStack.copy();

            // Player Inventory (0-35)
            if (pIndex < 36) {
                // If it's a StackUpgrade, try to move to Slot 38 (The Upgrade Slot)
                if (sourceStack.getItem() instanceof StackUpgradeItem) {
                    if (!moveItemStackTo(sourceStack, 38, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // Custom Slots (36, 37, 38) -> Move to Player Inventory
            else {
                if (!moveItemStackTo(sourceStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (sourceStack.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            } else {
                sourceSlot.setChanged();
            }

            if (sourceStack.getCount() == copyOfSourceStack.getCount()) {
                return ItemStack.EMPTY;
            }

            sourceSlot.onTake(playerIn, sourceStack);
        }

        return copyOfSourceStack;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }
}