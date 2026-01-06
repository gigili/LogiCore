package dev.gacbl.logicore.blocks.compiler;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerMenu;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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

    private static final int CYCLES_PROCESSED_PER_TICK = Config.COMPILER_CYCLES_PROCESSED_PER_TICK.get();

    private float rotation;

    private long currentCycles = 0;
    private int progress = 0;
    private int maxProgress = Config.COMPILER_MAX_PROGRESS.get();

    public long getCurrentCycles() {
        return this.currentCycles;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> CompilerBlockEntity.this.progress;
                case 1 -> CompilerBlockEntity.this.maxProgress;
                case 2 -> CompilerBlockEntity.this.upgradeItemHandler.getStackInSlot(0).getCount();
                default -> 0;
            };
        }

        @Override
        public void set(int pIndex, int pValue) {
            if (pIndex == 0) {
                CompilerBlockEntity.this.progress = pValue;
            } else if (pIndex == 1) {
                CompilerBlockEntity.this.maxProgress = pValue;
            }
        }

        @Override
        public int getCount() {
            return 3;
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
            ResourceLocation itemRes = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return slot == INPUT_SLOT && CycleValueManager.hasCycleValue(stack) && ClientKnowledgeData.isUnlocked(itemRes.toString());
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT_SLOT ? 1 : super.getSlotLimit(slot);
        }
    };

    private final ItemStackHandler upgradeItemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof StackUpgradeItem;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 16;
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
        return automationOutputHandler;
    }

    public IItemHandler getUpgradeItemHandler(@Nullable Direction side) {
        return upgradeItemHandler;
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
            int upgradeCount = upgradeItemHandler.getStackInSlot(0).getCount();
            return cost * ((upgradeCount > 0) ? (upgradeCount * 4L) : 1);
        }
        return 0;
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        long cap = getCycleDemand() + 100_000;
        long space = cap - currentCycles;
        long accepted = Math.min(maxReceive, space);

        if (!simulate && accepted > 0) {
            this.currentCycles += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public long extractCycles(long maxReceive, boolean simulate) {
        return 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CompilerBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack template = be.itemHandler.getStackInSlot(INPUT_SLOT);
        if (template.isEmpty() || !CycleValueManager.hasCycleValue(template)) {
            if (be.progress > 0) {
                be.progress = 0;
                be.maxProgress = 20;
                be.setChanged();
            }
            return;
        }

        long cost = be.getCycleDemand();

        int rawDuration = (int) Math.ceil((double) cost / CYCLES_PROCESSED_PER_TICK);

        int calculatedDuration = Math.max(20, Math.min(Config.COMPILER_MAX_TICK_DURATION.get(), rawDuration));

        if (be.maxProgress != calculatedDuration) {
            be.maxProgress = calculatedDuration;
            be.setChanged();
        }

        if (be.currentCycles >= cost && be.canInsertOutput(template)) {
            be.progress++;

            if (be.progress >= be.maxProgress) {
                be.currentCycles -= cost;
                int upgradeCount = be.upgradeItemHandler.getStackInSlot(0).getCount();
                ItemStack result = template.copy();
                if (upgradeCount > 0) {
                    result.setCount(upgradeCount * 4);
                } else {
                    result.setCount(1);
                }
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

        int outputCount = outputStack.getCount();
        int upgradeCount = upgradeItemHandler.getStackInSlot(0).getCount();

        if (upgradeCount == 0) {
            return outputCount < outputStack.getMaxStackSize();
        } else {
            return (outputCount + (upgradeCount * 4)) <= outputStack.getMaxStackSize();
        }
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
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), upgradeItemHandler.getStackInSlot(0));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeItemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putLong("currentCycles", currentCycles);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("upgrades")) {
            upgradeItemHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
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
                    manager.getNetworks().get(networkID).requestCycles(getCycleDemand());
                }
            }
        }
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }
}
