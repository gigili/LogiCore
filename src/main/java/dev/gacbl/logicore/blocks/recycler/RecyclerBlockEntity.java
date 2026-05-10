package dev.gacbl.logicore.blocks.recycler;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.recycler.ui.RecyclerMenu;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecyclerBlockEntity extends BlockEntity implements GeoBlockEntity, ICycleProvider, MenuProvider {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final SimpleEnergyHandler energyHandler = new SimpleEnergyHandler(10_000, 2000, 0);
    private final CycleStorage cycleStorage = new CycleStorage(1_000_000_000, 1_000_000_000, 1_000_000_000);
    private long internalCycleBuffer = 0;

    private int progress = 0;
    private int maxProgress = Config.RECYCLE_TIME.get();

    public RecyclerBlockEntity(BlockPos pos, BlockState blockState) {
        super(RecyclerModule.RECYCLER_BE.get(), pos, blockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationTest<RecyclerBlockEntity> state) {
        RecyclerBlockEntity be = state.animatable();
        if (be.getLevel() != null) {
            BlockState blockState = be.getLevel().getBlockState(be.getBlockPos());
            if (blockState.getValue(RecyclerModule.CRUSHING)) {
                state.setAndContinue(RawAnimation.begin().then("crushing", LoopType.LOOP));
            } else {
                state.setAndContinue(RawAnimation.begin().then("idle", LoopType.LOOP));
            }
        } else {
            state.setAndContinue(RawAnimation.begin().then("idle", LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(2) {
        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            setChanged();
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return slot == 0 ? 16 : 64;
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            ItemStack stack = resource.toStack();
            if (slot == 0) {
                return stack.getItem() instanceof StackUpgradeItem;
            } else if (slot == 1) {
                Identifier itemRes = BuiltInRegistries.ITEM.getKey(stack.getItem());
                long value = CycleValueManager.getCycleValue(stack);
                return value > 0 && ClientKnowledgeData.isUnlocked(itemRes.toString()) && (cycleStorage.getCycleCapacity() - cycleStorage.getCyclesAvailable()) >= value;
            }
            return false;
        }
    };

    public ItemStacksResourceHandler getItemHandler() {
        return itemHandler;
    }

    public SimpleEnergyHandler getEnergyHandler() {
        return energyHandler;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output.child("inventory"));
        energyHandler.serialize(output.child("energy"));
        output.putLong("internalCycleBuffer", internalCycleBuffer);
        output.putInt("progress", progress);
        output.putLong("cycles", cycleStorage.getCyclesAvailable());
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        cycleStorage.receiveCycles(input.getLongOr("cycles", 0L), false);
        input.child("inventory").ifPresent(itemHandler::deserialize);
        input.child("energy").ifPresent(energyHandler::deserialize);
        internalCycleBuffer = input.getLongOr("internalCycleBuffer", 0L);
        progress = input.getIntOr("progress", 0);
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.copyToList().get(0));
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
        var slots = itemHandler.copyToList();
        ItemStack slot1 = slots.get(1);
        if (energyHandler.getAmountAsInt() == 0 || energyHandler.getAmountAsInt() < Config.RECYCLE_FE_COST_PER_ITEM.get() || slot1.isEmpty()) {
            progress = 0;
            maxProgress = 20;
            toggleCrushingState(false);
            return;
        }

        if ((cycleStorage.getCycleCapacity() - cycleStorage.getCyclesAvailable()) < CycleValueManager.getCycleValue(slot1.copyWithCount(1))) {
            progress = 0;
            maxProgress = 20;
            toggleCrushingState(false);
            return;
        }

        if (slot1.isEmpty()) {
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

        ItemStack upgradeStack = slots.get(0);

        int maxPerCycle = upgradeStack.isEmpty() ? 1 : (upgradeStack.getCount() * 4);
        int itemsToProcess = Math.min(slot1.getCount(), maxPerCycle);

        long singleItemValue = CycleValueManager.getCycleValue(slot1.copyWithCount(1));
        long totalCycleValue = singleItemValue * itemsToProcess;
        long feCost = (long) Config.RECYCLE_FE_COST_PER_ITEM.get() * itemsToProcess;

        if (progress >= maxProgress) {
            progress = 0;
            maxProgress = Config.RECYCLE_TIME.get();
            if (!slot1.isEmpty()) {
                long inserted = cycleStorage.receiveCycles(totalCycleValue, false);

                if (inserted < totalCycleValue) {
                    internalCycleBuffer += (totalCycleValue - inserted);
                }

                ItemResource resource = itemHandler.getResource(1);
                if (resource != null) {
                    try (Transaction tx = Transaction.openRoot()) {
                        itemHandler.extract(1, resource, itemsToProcess, tx);
                        tx.commit();
                    }
                }
                try (Transaction tx = Transaction.openRoot()) {
                    energyHandler.extract((int) feCost, tx);
                    tx.commit();
                }
            }
        } else if (Config.RECYCLE_EACH_TICK_CONSUMES_FE.get() && !slot1.isEmpty()) {
            try (Transaction tx = Transaction.openRoot()) {
                energyHandler.extract((int) feCost, tx);
                tx.commit();
            }
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
