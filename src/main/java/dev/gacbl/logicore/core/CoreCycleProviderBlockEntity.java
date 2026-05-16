package dev.gacbl.logicore.core;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.server.ServerItem;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncCycleDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public abstract class CoreCycleProviderBlockEntity extends BlockEntity implements ICycleProvider {
    protected int BASE_CYCLE_GENERATION;
    protected int CYCLES_PER_PROCESSOR;
    protected int FE_PER_CYCLE;

    protected SimpleEnergyHandler energyHandler;
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
        energyHandler = new SimpleEnergyHandler(feCapacity, 100_000, 100_000);
        cycleStorage = new CycleStorage(cycleCapacity);
        this.dataCenterBoost = dataCenterBoost;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return (int) switch (index) {
                case 0 -> CoreCycleProviderBlockEntity.this.energyHandler.getAmountAsInt();
                case 1 -> CoreCycleProviderBlockEntity.this.energyHandler.getCapacityAsInt();
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
        ItemStacksResourceHandler handler = getItemHandler();
        if (handler != null) {
            var slots = handler.copyToList();
            for (int i = 0; i < slots.size(); i++) {
                var stack = slots.get(i);
                if (stack.getItem() instanceof ProcessorUnitItem processor) {
                    cyclesToGenerate += processor.tier.cycleRate.get();
                } else if (stack.getItem() instanceof ServerItem server) {
                    ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                    for (var innerStack : (Iterable<ItemStack>) contents.nonEmptyItemCopyStream()::iterator) {
                        if (innerStack.getItem() instanceof ProcessorUnitItem processor) {
                            cyclesToGenerate += processor.tier.cycleRate.get();
                        }
                    }
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
    public long receiveCycles(long maxReceive, boolean simulate) {
        return this.cycleStorage.receiveCycles(maxReceive, simulate);
    }

    public SimpleEnergyHandler getEnergyHandler() {
        return this.energyHandler;
    }

    public abstract ItemStacksResourceHandler getItemHandler();

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        energyHandler.serialize(output.child("energy"));
        output.putLong("cycles", cycleStorage.getCyclesAvailable());
        output.putBoolean("isGenerating", isGenerating);
        if (dataCenterController != null) {
            output.putLong("dataCenterController", dataCenterController.asLong());
        }
        output.putBoolean("hasDataCenterBoost", hasDataCenterBoost);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.child("energy").ifPresent(energyHandler::deserialize);
        cycleStorage.receiveCycles(input.getLongOr("cycles", 0L), false);
        isGenerating = input.getBooleanOr("isGenerating", false);
        long dataCenterControllerLong = input.getLongOr("dataCenterController", 0L);
        if (dataCenterControllerLong != 0L) {
            dataCenterController = BlockPos.of(dataCenterControllerLong);
        }
        hasDataCenterBoost = input.getBooleanOr("hasDataCenterBoost", false);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreCycleProviderBlockEntity be) {
        if (level.isClientSide()) return;
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
        if (this.level == null || this.level.isClientSide() || !canGenerate()) {
            isGenerating = false;
            return;
        }

        if (this.cycleStorage.getCyclesAvailable() >= this.cycleStorage.getCycleCapacity()) {
            isGenerating = false;
            return;
        }
        if (this.energyHandler.getAmountAsLong() < FE_PER_CYCLE) {
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

        if (this.energyHandler.getAmountAsLong() >= feCost) {
            isGenerating = true;
            try (Transaction tx = Transaction.openRoot()) {
                this.energyHandler.extract((int) feCost, tx);
                tx.commit();
            }
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

    public void setDataCenterController(BlockPos controllerPos) {
        this.dataCenterController = controllerPos;
        setChanged();
    }

    public void setClientData(int energy, long cycles, boolean isGenerating) {
        int diff = energy - this.energyHandler.getAmountAsInt();
        if (diff > 0) {
            try (Transaction tx = Transaction.openRoot()) {
                this.energyHandler.insert(diff, tx);
                tx.commit();
            }
        } else if (diff < 0) {
            try (Transaction tx = Transaction.openRoot()) {
                this.energyHandler.extract(-diff, tx);
                tx.commit();
            }
        }
        
        long currentCycles = this.cycleStorage.getCyclesAvailable();
        if (cycles > currentCycles) {
            this.cycleStorage.receiveCycles(cycles - currentCycles, false);
        } else {
            this.cycleStorage.extractCycles(currentCycles - cycles, false);
        }

        this.isGenerating = isGenerating;
    }

    public void syncData() {
        if (this.level != null && !this.level.isClientSide()) {
            PacketHandler.sendToClientsTrackingChunk(
                    (net.minecraft.server.level.ServerLevel) this.level,
                    this.getBlockPos(),
                    new SyncCycleDataPayload(this.worldPosition, this.energyHandler.getAmountAsInt(), this.cycleStorage.getCyclesAvailable(), this.isGenerating)
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
