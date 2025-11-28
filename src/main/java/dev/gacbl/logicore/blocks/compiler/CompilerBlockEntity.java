package dev.gacbl.logicore.blocks.compiler;

import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerMenu;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CompilerBlockEntity extends BlockEntity implements ICycleConsumer, MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private static final int PROCESSING_TIME = 20;

    private long currentCycles = 0;
    private int progress = 0;

    public long getCurrentCycles() {
        return this.currentCycles;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> CompilerBlockEntity.this.progress;
                case 1 -> PROCESSING_TIME;
                default -> 0;
            };
        }

        @Override
        public void set(int pIndex, int pValue) {
            if (pIndex == 0) {
                CompilerBlockEntity.this.progress = pValue;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CompilerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CompilerModule.COMPILER_BLOCK_ENTITY.get(), pos, blockState);
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == INPUT_SLOT && !this.getStackInSlot(INPUT_SLOT).isEmpty()) {
                requestCycles();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT && CycleValueManager.hasCycleValue(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT_SLOT ? 1 : super.getSlotLimit(slot);
        }
    };

    private final IItemHandler automationInputHandler = new RangedWrapper(itemHandler, INPUT_SLOT, INPUT_SLOT + 1) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };

    private final IItemHandler automationOutputHandler = new RangedWrapper(itemHandler, OUTPUT_SLOT, OUTPUT_SLOT + 1) {
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }
    };

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) return itemHandler;
        if (side == Direction.DOWN) return automationOutputHandler;
        return automationInputHandler;
    }

    @Override
    public long getCyclesStored() {
        return this.currentCycles;
    }

    @Override
    public long getCycleDemand() {
        ItemStack template = itemHandler.getStackInSlot(INPUT_SLOT);
        if (template.isEmpty()) return 0;

        int cost = CycleValueManager.getCycleValue(template);
        if (cost <= 0) return 0;

        if (canInsertOutput(template)) {
            return cost;
        }
        return 0;
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        long cap = 1000000;
        long space = cap - currentCycles;
        long accepted = Math.min(maxReceive, space);

        if (!simulate && accepted > 0) {
            this.currentCycles += accepted;
            setChanged();
        }
        return accepted;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CompilerBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack template = be.itemHandler.getStackInSlot(INPUT_SLOT);
        if (template.isEmpty() || !CycleValueManager.hasCycleValue(template)) {
            if (be.progress > 0) {
                be.progress = 0;
                be.setChanged();
            }
            return;
        }

        int cost = CycleValueManager.getCycleValue(template);

        if (be.currentCycles >= cost && be.canInsertOutput(template)) {
            be.progress++;

            if (be.progress >= PROCESSING_TIME) {
                be.currentCycles -= cost;

                ItemStack result = template.copy();
                result.setCount(1);
                be.addToOutput(result);

                be.progress = 0;
            }
            be.setChanged();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.setChanged();
            }
        }
    }

    private boolean canInsertOutput(ItemStack template) {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) return true;

        if (!ItemStack.isSameItemSameComponents(outputStack, template)) return false;

        return outputStack.getCount() < outputStack.getMaxStackSize();
    }

    private void addToOutput(ItemStack result) {
        ItemStack existing = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            existing.grow(result.getCount());
        }
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(OUTPUT_SLOT));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putLong("currentCycles", currentCycles);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = tag.getInt("progress");
        currentCycles = tag.getLong("currentCycles");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.compiler");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new CompilerMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @Nullable ICycleConsumer getCycleStorage() {
        return this;
    }

    private void requestCycles() {
        if (level == null || level.isClientSide || level.getServer() == null) return;
        NetworkManager manager = NetworkManager.get(level.getServer().overworld());
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof DataCableBlockEntity dcbe) {
                UUID networkID = dcbe.getNetworkUUID();
                if (networkID != null && manager.getNetworks().containsKey(networkID)) {
                    manager.getNetworks().get(networkID).setDirty();
                }
            }
        }
    }
}
