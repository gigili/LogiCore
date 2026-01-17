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
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CompilerMenu extends MyAbstractContainerMenu {
    public static final int TEMPLATE_SLOT_INDEX = 36;

    public CompilerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(3));
    }

    public CompilerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(CompilerModule.COMPILER_MENU.get(), containerId, playerInventory, entity, data);

        this.addSlot(new SlotItemHandler(((CompilerBlockEntity) this.blockEntity).getItemHandler(null), 0, 72, 82) {
            @Override
            public boolean mayPickup(@NotNull Player playerIn) {
                return false;
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(((CompilerBlockEntity) this.blockEntity).getItemHandler(null), 1, 144, 82) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(((CompilerBlockEntity) this.blockEntity).getUpgradeItemHandler(null), 0, 108, 118));
    }

    public CompilerMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        super.quickMoveStack(playerIn, pIndex);

        Slot sourceSlot = slots.get(pIndex);
        if (pIndex == 37 || pIndex == 38) {
            if (sourceSlot.hasItem()) {
                ItemStack source = sourceSlot.getItem();
                ItemStack copy = source.copy();
                if (!moveItemStackTo(source, 0, 36, true)) return ItemStack.EMPTY;
                sourceSlot.onTake(playerIn, source);
                return copy;
            }
        } else {
            if (sourceSlot.hasItem()) {
                ItemStack source = sourceSlot.getItem();
                if (source.getItem() instanceof StackUpgradeItem) {
                    ItemStack copy = source.copy();
                    if (!moveItemStackTo(source, 38, 39, true)) return ItemStack.EMPTY;
                    sourceSlot.onTake(playerIn, source);
                    return copy;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }
}
