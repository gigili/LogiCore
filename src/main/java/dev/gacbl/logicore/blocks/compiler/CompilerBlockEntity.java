package dev.gacbl.logicore.blocks.compiler;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerMenu;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
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
                case 2 -> CompilerBlockEntity.this.upgradeItemHandler.copyToList().get(0).getCount();
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

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(2) {
        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            setChanged();
            if (slot == INPUT_SLOT && !stack.isEmpty()) {
                requestCycles();
            }
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return slot == INPUT_SLOT ? 1 : 64;
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            ItemStack stack = resource.toStack();
            return slot == INPUT_SLOT && CycleValueManager.hasCycleValue(stack) && ClientKnowledgeData.isUnlocked(Utils.getItemKey(stack));
        }
    };

    private final ItemStacksResourceHandler upgradeItemHandler = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            setChanged();
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return 16;
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            return resource.toStack().getItem() instanceof StackUpgradeItem;
        }
    };

    private final ResourceHandler<ItemResource> automationOutputHandler = new RangedResourceHandler<>(itemHandler, OUTPUT_SLOT, OUTPUT_SLOT + 1) {
        @Override
        public int insert(ItemResource resource, int amount, TransactionContext tx) {
            return 0; // Prevent insertion into output via automation
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext tx) {
            return 0;
        }
    };

    public ItemStacksResourceHandler getInternalItemHandler() {
        return itemHandler;
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (side == null) return itemHandler;
        return automationOutputHandler;
    }

    public ItemStacksResourceHandler getUpgradeItemHandler(@Nullable Direction side) {
        return upgradeItemHandler;
    }

    @Override
    public long getCyclesStored() {
        return this.currentCycles;
    }

    @Override
    public long getCycleDemand() {
        ItemStack template = itemHandler.copyToList().get(INPUT_SLOT);
        if (template.isEmpty()) return 0;

        int cost = CycleValueManager.getCycleValue(template);
        if (cost <= 0) return 0;

        if (canInsertOutput(template)) {
            int upgradeCount = upgradeItemHandler.copyToList().get(0).getCount();
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
        if (level.isClientSide()) return;

        boolean wasWorking = be.progress > 0;

        ItemStack template = be.itemHandler.copyToList().get(INPUT_SLOT);
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
                int upgradeCount = be.upgradeItemHandler.copyToList().get(0).getCount();
                ItemStack result = template.copy();
                int targetCount = upgradeCount > 0 ? upgradeCount * 4 : 1;
                result.setCount(Math.min(targetCount, template.getMaxStackSize()));
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

        boolean isWorking = be.progress > 0;
        if (wasWorking != isWorking) {
            be.sendUpdatePacket();
        }
    }

    private boolean canInsertOutput(ItemStack template) {
        ItemStack outputStack = itemHandler.copyToList().get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) return true;

        if (!ItemStack.isSameItemSameComponents(outputStack, template)) return false;

        int outputCount = outputStack.getCount();
        int upgradeCount = upgradeItemHandler.copyToList().get(0).getCount();

        if (upgradeCount == 0) {
            return outputCount < outputStack.getMaxStackSize();
        } else {
            return (outputCount + (upgradeCount * 4)) <= outputStack.getMaxStackSize();
        }
    }

    private void addToOutput(ItemStack result) {
        ItemStack existing = itemHandler.copyToList().get(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            itemHandler.set(OUTPUT_SLOT, ItemResource.of(result), result.getCount());
        } else {
            ItemStack combined = existing.copy();
            combined.grow(result.getCount());
            itemHandler.set(OUTPUT_SLOT, ItemResource.of(combined), combined.getCount());
        }
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.copyToList().get(OUTPUT_SLOT));
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), upgradeItemHandler.copyToList().get(0));
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output.child("inventory"));
        upgradeItemHandler.serialize(output.child("upgrades"));
        output.putInt("progress", progress);
        output.putLong("currentCycles", currentCycles);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.child("inventory").ifPresent(itemHandler::deserialize);
        input.child("upgrades").ifPresent(upgradeItemHandler::deserialize);
        progress = input.getIntOr("progress", 0);
        currentCycles = input.getLongOr("currentCycles", 0);
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
        if (level == null || level.isClientSide() || level.getServer() == null) return;
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

    public float getProgress(float partialTick) {
        return (float) this.progress / (float) Math.max(1, this.maxProgress);
    }

    public boolean isWorking() {
        return this.progress > 0 && this.currentCycles > 0;
    }

    private void sendUpdatePacket() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
