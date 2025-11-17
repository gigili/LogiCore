package dev.gacbl.logicore.core;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CoreCycleProviderBlockEntity extends BlockEntity implements ICycleProvider {
    private static int BASE_CYCLE_GENERATION;
    private static int CYCLES_PER_PROCESSOR;
    private static int FE_PER_CYCLE;

    private static int CYCLE_CAPACITY;
    private static int FE_CAPACITY;

    private EnergyStorage energyStorage;
    private CycleStorage cycleStorage;

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
            return 4;
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
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreCycleProviderBlockEntity be) {
        if (level.isClientSide) return;
        be.generateCycles();
    }

    private void generateCycles() {
        if (this.level == null) return;

        if (this.cycleStorage.getCyclesAvailable() >= this.cycleStorage.getCycleCapacity()) return;
        if (this.energyStorage.getEnergyStored() < FE_PER_CYCLE) return;

        int processorCount = getProcessorCount();

        if (processorCount == 0) return;

        long cyclesToGenerate = BASE_CYCLE_GENERATION + ((long) processorCount * CYCLES_PER_PROCESSOR);
        long feCost = cyclesToGenerate * FE_PER_CYCLE;

        if (this.energyStorage.extractEnergy((int) feCost, true) == feCost) {
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
