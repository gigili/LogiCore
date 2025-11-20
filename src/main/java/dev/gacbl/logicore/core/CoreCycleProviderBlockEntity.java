package dev.gacbl.logicore.core;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CoreCycleProviderBlockEntity extends BlockEntity implements ICycleProvider {
    private int BASE_CYCLE_GENERATION;
    private int CYCLES_PER_PROCESSOR;
    private int FE_PER_CYCLE;

    private int CYCLE_CAPACITY;
    private int FE_CAPACITY;

    private EnergyStorage energyStorage;
    private CycleStorage cycleStorage;

    public boolean isGenerating = false;

    public CoreCycleProviderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public CoreCycleProviderBlockEntity(Integer baseCycleGeneration, Integer cyclePerProcessor, Integer fePerCycle, Integer cycleCapacity, Integer feCapacity, BlockEntityType<? extends CoreCycleProviderBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        this(blockEntityType, pos, state);
        BASE_CYCLE_GENERATION = baseCycleGeneration;
        CYCLES_PER_PROCESSOR = cyclePerProcessor;
        FE_PER_CYCLE = fePerCycle;
        CYCLE_CAPACITY = cycleCapacity;
        FE_CAPACITY = feCapacity;
        energyStorage = new EnergyStorage(FE_CAPACITY);
        cycleStorage = new CycleStorage(CYCLE_CAPACITY);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return (int) switch (index) {
                case 0 -> CoreCycleProviderBlockEntity.this.energyStorage.getEnergyStored();
                case 1 -> CoreCycleProviderBlockEntity.this.energyStorage.getMaxEnergyStored();
                case 2 -> CoreCycleProviderBlockEntity.this.cycleStorage.getCyclesAvailable();
                case 3 -> CoreCycleProviderBlockEntity.this.cycleStorage.getCycleCapacity();
                case 4 -> CoreCycleProviderBlockEntity.this.BASE_CYCLE_GENERATION;
                case 5 -> CoreCycleProviderBlockEntity.this.CYCLES_PER_PROCESSOR;
                case 6 -> CoreCycleProviderBlockEntity.this.FE_PER_CYCLE;
                case 7 -> getProcessorCount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // We generally don't set energy from the client side GUI, so this can be empty
            // or used if you need specific logic.
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public ContainerData getContainerData() {
        return this.data;
    }

    public int getProcessorCount() {
        return 0;
    }

    @Override
    public long getCyclesAvailable() {
        return this.cycleStorage.getCyclesAvailable();
    }

    @Override
    public long getCycleCapacity() {
        return this.cycleStorage.getCycleCapacity();
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        return this.cycleStorage.extractCycles(maxExtract, simulate);
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        return this.cycleStorage.receiveCycles(receive, simulate);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", this.energyStorage.serializeNBT(registries));
        tag.put("cycles", this.cycleStorage.serializeNBT(registries));
        tag.putBoolean("isGenerating", isGenerating);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy", 3) && tag.get("energy") != null) {
            this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("energy")));
        }
        if (tag.contains("cycles", 10)) {
            this.cycleStorage.deserializeNBT(registries, (CompoundTag) tag.get("cycles"));
        }

        if (tag.contains("isGenerating")) {
            isGenerating = tag.getBoolean("isGenerating");
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreCycleProviderBlockEntity be) {
        if (level.isClientSide) return;
        be.generateCycles();

        if (state.hasProperty(ServerRackModule.GENERATING)) {
            boolean currentGeneratingState = state.getValue(ServerRackModule.GENERATING);

            if (be.isGenerating != currentGeneratingState) {
                level.setBlock(pos, state.setValue(ServerRackModule.GENERATING, be.isGenerating), 3);
                if (state.hasProperty(ServerRackBlock.HALF)) {
                    updateOtherHalf(level, pos, state, be.isGenerating);
                }
            }
        }
    }

    private static void updateOtherHalf(Level level, BlockPos pos, BlockState state, boolean isWorking) {
        DoubleBlockHalf half = state.getValue(ServerRackBlock.HALF);
        BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();

        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.getBlock() instanceof ServerRackBlock) {
            level.setBlock(otherPos, otherState.setValue(ServerRackModule.GENERATING, isWorking), 3);
        }
    }

    private void generateCycles() {
        if (this.level == null || this.level.isClientSide) {
            isGenerating = false;
            return;
        }

        boolean isReceivingRedstoneSignal = level.hasNeighborSignal(this.getBlockPos());

        if (isReceivingRedstoneSignal) {
            isGenerating = false;
            return;
        }

        if (this.cycleStorage.getCyclesAvailable() >= this.cycleStorage.getCycleCapacity()) {
            isGenerating = false;
            return;

        }
        if (this.energyStorage.getEnergyStored() < FE_PER_CYCLE) {
            isGenerating = false;
            return;
        }

        int processorCount = getProcessorCount();

        if (processorCount == 0) {
            isGenerating = false;
            return;
        }

        long cyclesToGenerate = BASE_CYCLE_GENERATION + ((long) processorCount * CYCLES_PER_PROCESSOR);
        long feCost = cyclesToGenerate * FE_PER_CYCLE;

        if (this.energyStorage.extractEnergy((int) feCost, true) == feCost) {
            isGenerating = true;
            this.energyStorage.extractEnergy((int) feCost, false);
            this.cycleStorage.receiveCycles(cyclesToGenerate, false);
            setChanged();
        }
    }

    public ICycleStorage getCycleStorage() {
        return this.cycleStorage;
    }

    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public ItemStackHandler getItemHandler() {
        return null;
    }
}
