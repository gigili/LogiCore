package dev.gacbl.logicore.core;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncCycleDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

public abstract class CoreCycleProviderBlockEntity extends BlockEntity implements ICycleProvider {
    protected int BASE_CYCLE_GENERATION;
    protected int CYCLES_PER_PROCESSOR;
    protected int FE_PER_CYCLE;

    protected EnergyStorage energyStorage;
    protected CycleStorage cycleStorage;

    public boolean isGenerating = false;

    public BlockPos dataCenterController = null;

    protected boolean hasDataCenterBoost = false;

    protected int dataCenterBoost = 0;

    protected int cachedProcessorCount = 0;

    public CoreCycleProviderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public CoreCycleProviderBlockEntity(Integer baseCycleGeneration, Integer cyclePerProcessor, Integer fePerCycle, Integer cycleCapacity, Integer feCapacity, Integer dataCenterBoost, BlockEntityType<? extends CoreCycleProviderBlockEntity> blockEntityType, BlockPos pos, BlockState state) {
        this(blockEntityType, pos, state);
        BASE_CYCLE_GENERATION = baseCycleGeneration;
        CYCLES_PER_PROCESSOR = cyclePerProcessor;
        FE_PER_CYCLE = fePerCycle;
        energyStorage = new EnergyStorage(feCapacity, 100_000, 100_000);
        cycleStorage = new CycleStorage(cycleCapacity);
        this.dataCenterBoost = dataCenterBoost;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return (int) switch (index) {
                case 0 -> CoreCycleProviderBlockEntity.this.energyStorage.getEnergyStored();
                case 1 -> CoreCycleProviderBlockEntity.this.energyStorage.getMaxEnergyStored();
                case 2 -> CoreCycleProviderBlockEntity.this.cycleStorage.getCyclesAvailable();
                case 3 -> CoreCycleProviderBlockEntity.this.cycleStorage.getCycleCapacity();
                case 4 -> CoreCycleProviderBlockEntity.this.calculateBaseCycleGeneration();
                case 5 -> CoreCycleProviderBlockEntity.this.CYCLES_PER_PROCESSOR;
                case 6 -> CoreCycleProviderBlockEntity.this.FE_PER_CYCLE;
                case 7 -> getProcessorCount();
                case 8 -> CoreCycleProviderBlockEntity.this.hasDataCenterBoost ? 1 : 0;
                case 9 -> CoreCycleProviderBlockEntity.this.dataCenterBoost;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public int calculateBaseCycleGeneration() {
        long cyclesToGenerate = BASE_CYCLE_GENERATION;
        ItemStackHandler handler = getItemHandler();
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                var stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof ProcessorUnitItem processor) {
                    cyclesToGenerate += processor.tier.cycleRate.get();
                }
            }
        }
        return (int) cyclesToGenerate;
    }

    public abstract int getProcessorCount();

    public abstract int getMaxProcessorCount();

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
        if (dataCenterController != null) {
            tag.put("dataCenterController", NbtUtils.writeBlockPos(dataCenterController));
        }
        tag.putBoolean("hasDataCenterBoost", hasDataCenterBoost);
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

        if (tag.contains("dataCenterController")) {
            dataCenterController = NbtUtils.readBlockPos(tag, "dataCenterController").orElse(null);
        }

        hasDataCenterBoost = tag.getBoolean("hasDataCenterBoost");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreCycleProviderBlockEntity be) {
        if (level.isClientSide) return;
        be.hasDataCenterBoost = false;
        if (be.dataCenterController != null) {
            if (level.getBlockEntity(be.dataCenterController) instanceof AbstractSealedController abc) {
                if (abc.isFormed) {
                    be.hasDataCenterBoost = true;
                }
            }
        }
        be.generateCycles();

        if (state.hasProperty(ServerRackBlock.GENERATING)) {
            boolean currentGeneratingState = state.getValue(ServerRackBlock.GENERATING);

            if (be.isGenerating != currentGeneratingState) {
                level.setBlock(pos, state.setValue(ServerRackBlock.GENERATING, be.isGenerating), 3);
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
            level.setBlock(otherPos, otherState.setValue(ServerRackBlock.GENERATING, isWorking), 3);
        }
    }

    protected boolean canGenerate() {
        return true;
    }

    private void generateCycles() {
        if (this.level == null || this.level.isClientSide || !canGenerate()) {
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

        long cyclesToGenerate = calculateBaseCycleGeneration();

        long feCost = Math.min(cyclesToGenerate * FE_PER_CYCLE, 99_999L);
        if (hasDataCenterBoost) {
            cyclesToGenerate += dataCenterBoost;
        }

        boolean prevGenerating = isGenerating;

        if (this.energyStorage.extractEnergy((int) feCost, true) == feCost) {
            isGenerating = true;
            this.energyStorage.extractEnergy((int) feCost, false);
            this.cycleStorage.receiveCycles(cyclesToGenerate, false);
            setChanged();
        } else {
            isGenerating = false;
        }

        if (prevGenerating != isGenerating) {
            syncData();
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

    public void setDataCenterController(BlockPos controllerPos) {
        this.dataCenterController = controllerPos;
        setChanged();
    }

    public void setClientData(int energy, long cycles, boolean isGenerating) {
        this.energyStorage.receiveEnergy(energy - this.energyStorage.getEnergyStored(), false);
        long currentCycles = this.cycleStorage.getCyclesAvailable();
        if (cycles > currentCycles) {
            this.cycleStorage.receiveCycles(cycles - currentCycles, false);
        } else {
            this.cycleStorage.extractCycles(currentCycles - cycles, false);
        }

        this.isGenerating = isGenerating;
    }

    public void syncData() {
        if (this.level != null && !this.level.isClientSide) {
            PacketHandler.sendToClientsTrackingChunk(
                    this.level,
                    this.getBlockPos(),
                    new SyncCycleDataPayload(this.worldPosition, this.energyStorage.getEnergyStored(), this.cycleStorage.getCyclesAvailable(), this.isGenerating)
            );
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
