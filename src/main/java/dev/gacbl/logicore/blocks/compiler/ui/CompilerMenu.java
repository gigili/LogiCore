package dev.gacbl.logicore.blocks.compiler.ui;

import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CompilerMenu extends AbstractContainerMenu {
    public final CompilerBlockEntity blockEntity;
    private final ContainerData data;

    public static final int TEMPLATE_SLOT_INDEX = 36;

    public CompilerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(3));
    }

    public CompilerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(CompilerModule.COMPILER_MENU.get(), containerId);
        this.blockEntity = (CompilerBlockEntity) entity;
        this.data = data;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(null), 0, 72, 82) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(null), 1, 144, 82) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(this.blockEntity.getUpgradeItemHandler(null), 0, 108, 118));

        addDataSlots(data);
    }

    public CompilerMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        if (pIndex == 37 || pIndex == 38) {
            Slot sourceSlot = slots.get(pIndex);
            if (sourceSlot != null && sourceSlot.hasItem()) {
                ItemStack source = sourceSlot.getItem();
                ItemStack copy = source.copy();
                if (!moveItemStackTo(source, 0, 36, true)) return ItemStack.EMPTY;
                sourceSlot.onTake(playerIn, source);
                return copy;
            }
        } else {
            Slot sourceSlot = slots.get(pIndex);
            if (sourceSlot != null && sourceSlot.hasItem()) {
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        Level level = player.level();
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, CompilerModule.COMPILER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 36 + l * 18, 157 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 36 + i * 18, 213));
        }
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public int getStackUpgradeCount() {
        return this.data.get(2);
    }
}
