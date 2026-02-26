package dev.gacbl.logicore.blocks.recycler;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.recycler.ui.RecyclerMenu;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;

import java.util.Objects;

public class RecyclerBlockEntity extends BlockEntity implements GeoBlockEntity, ICycleProvider, MenuProvider {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final EnergyStorage energyStorage = new EnergyStorage(10_000, 2000, 0);
    private final CycleStorage cycleStorage = new CycleStorage(1_000_000_000, 1_000_000_000, 1_000_000_000);
    private long internalCycleBuffer = 0;

    private int progress = 0;
    private int maxProgress = Config.RECYCLE_TIME.get();

    public RecyclerBlockEntity(BlockPos pos, BlockState blockState) {
        super(RecyclerModule.RECYCLER_BE.get(), pos, blockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<RecyclerBlockEntity> recyclerBlockEntityAnimationState) {
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            BlockEntity entity = level.getBlockEntity(worldPosition);
            if (entity instanceof RecyclerBlockEntity && state.getValue(RecyclerModule.CRUSHING)) {
                recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("crushing", Animation.LoopType.LOOP));
            } else {
                recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
            }
        } else {
            recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.getItem() instanceof StackUpgradeItem;
            } else if (slot == 1) {
                ResourceLocation itemRes = BuiltInRegistries.ITEM.getKey(stack.getItem());
                long value = CycleValueManager.getCycleValue(stack);
                return value > 0 && ClientKnowledgeData.isUnlocked(itemRes.toString()) && (cycleStorage.getCycleCapacity() - cycleStorage.getCyclesAvailable()) >= value;
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 16 : super.getSlotLimit(slot);
        }
    };

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("energyStorage", energyStorage.serializeNBT(registries));
        tag.putLong("internalCycleBuffer", internalCycleBuffer);
        tag.putInt("progress", progress);
        tag.putLong("cycles", cycleStorage.getCyclesAvailable());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("cycles")) {
            cycleStorage.receiveCycles(tag.getLong("cycles"), false);
        }

        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }

        if (tag.contains("energyStorage", 3) && tag.get("energyStorage") != null) {
            this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("energyStorage")));
        }

        if (tag.contains("internalCycleBuffer")) {
            internalCycleBuffer = tag.getLong("internalCycleBuffer");
        }

        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(0));
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, RecyclerBlockEntity recyclerBlockEntity) {
        if (level == null || level.isClientSide()) return;
        recyclerBlockEntity.serverTick((ServerLevel) level, blockPos, blockState);
    }

    private void toggleCrushingState(Boolean isCrushing) {
        if (level == null || level.isClientSide()) return;
        level.setBlock(worldPosition, getBlockState().setValue(RecyclerModule.CRUSHING, isCrushing), RecyclerBlock.UPDATE_ALL);
        setChanged();
    }

    public void serverTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        if (energyStorage.getEnergyStored() == 0 || energyStorage.getEnergyStored() < Config.RECYCLE_FE_COST_PER_ITEM.get() || itemHandler.getStackInSlot(1).isEmpty()) {
            progress = 0;
            maxProgress = 20;
            toggleCrushingState(false);
            return;
        }

        if ((cycleStorage.getCycleCapacity() - cycleStorage.getCyclesAvailable()) < CycleValueManager.getCycleValue(itemHandler.getStackInSlot(1).copyWithCount(1))) {
            progress = 0;
            maxProgress = 20;
            toggleCrushingState(false);
            return;
        }

        ItemStack template = itemHandler.getStackInSlot(1);
        if (template.isEmpty()) {
            if (progress > 0) {
                progress = 0;
                maxProgress = 20;
                toggleCrushingState(false);
            }
            return;
        }

        if (!blockState.getValue(RecyclerModule.CRUSHING)) {
            toggleCrushingState(true);
        }

        progress++;

        ItemStack itemStack = itemHandler.getStackInSlot(1);
        ItemStack upgradeStack = itemHandler.getStackInSlot(0);

        int maxPerCycle = upgradeStack.isEmpty() ? 1 : (upgradeStack.getCount() * 4);
        int itemsToProcess = Math.min(itemStack.getCount(), maxPerCycle);

        long singleItemValue = CycleValueManager.getCycleValue(itemStack.copyWithCount(1));
        long totalCycleValue = singleItemValue * itemsToProcess;
        long feCost = (long) Config.RECYCLE_FE_COST_PER_ITEM.get() * itemsToProcess;

        if (progress >= maxProgress) {
            progress = 0;
            maxProgress = Config.RECYCLE_TIME.get();
            if (!itemStack.isEmpty()) {
                long inserted = cycleStorage.receiveCycles(totalCycleValue, false);

                if (inserted < totalCycleValue) {
                    internalCycleBuffer += (totalCycleValue - inserted);
                }

                itemHandler.extractItem(1, itemsToProcess, false);
                energyStorage.extractEnergy((int) feCost, false);
            }
        } else if (Config.RECYCLE_EACH_TICK_CONSUMES_FE.get() && !itemHandler.getStackInSlot(1).isEmpty()) {
            energyStorage.extractEnergy((int) feCost, false);
        }

        if (internalCycleBuffer > 0 && cycleStorage.getCycleCapacity() - cycleStorage.getCyclesAvailable() > 0) {
            long extracted = cycleStorage.receiveCycles(internalCycleBuffer, false);
            internalCycleBuffer -= extracted;
        }

        if (internalCycleBuffer < 0) {
            internalCycleBuffer = 0;
        }
    }

    @Override
    public long getCyclesAvailable() {
        return cycleStorage.getCyclesAvailable();
    }

    @Override
    public long getCycleCapacity() {
        return cycleStorage.getCycleCapacity();
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        return cycleStorage.extractCycles(maxExtract, simulate);
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        return cycleStorage.receiveCycles(receive, simulate);
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.recycler");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new RecyclerMenu(containerId, inventory, this, this.data);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> RecyclerBlockEntity.this.progress;
                case 1 -> RecyclerBlockEntity.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
}
