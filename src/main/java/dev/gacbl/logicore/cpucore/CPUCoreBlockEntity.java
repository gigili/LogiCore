package dev.gacbl.logicore.cpucore;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import dev.gacbl.logicore.datacable.DataCableBlock;
import dev.gacbl.logicore.network.NetworkManager;
import dev.gacbl.logicore.serverrack.ServerRackBlock;
import dev.gacbl.logicore.serverrack.ServerRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CPUCoreBlockEntity extends BlockEntity {
    // --- Constants ---
    private static final int BASE_CYCLE_GENERATION = Config.CPU_CORE_BASE_CYCLE_GENERATION.get();
    private static final int CYCLES_PER_PROCESSOR = Config.CPU_CORE_CYCLES_PER_PROCESSOR.get();
    private static final int FE_PER_CYCLE = Config.CPU_CORE_FE_PER_CYCLE.get();
    public static final int MAX_RACKS = Config.CPU_CORE_MAX_RACKS.get();
    private static final int SCAN_INTERVAL = Config.CPU_CORE_SCAN_INTERVAL.get();

    // --- Energy & Cycle Storage ---
    private static final int CYCLE_CAPACITY = Config.CPU_CORE_CYCLE_CAPACITY.get();
    private static final int FE_CAPACITY = Config.CPU_CORE_FE_CAPACITY.get();
    private final EnergyStorage energyStorage = new EnergyStorage(FE_CAPACITY);
    private final CycleStorage cycleStorage = new CycleStorage(CYCLE_CAPACITY);

    // --- Multiblock ---
    private final List<BlockPos> connectedRacks = new ArrayList<>();
    private final List<BlockPos> cablesInQueue = new ArrayList<>();
    private int scanCooldown = SCAN_INTERVAL;

    public CPUCoreBlockEntity(BlockPos pos, BlockState state) {
        super(CPUCoreModule.CPU_CORE_BLOCK_ENTITY.get(), pos, state);
    }

    // --- Capability Getters ---
    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public ICycleStorage getCycleStorage() {
        return this.cycleStorage;
    }

    public List<BlockPos> getConnectedRacks() {
        return Collections.unmodifiableList(this.connectedRacks);
    }

    // --- Ticking Logic ---
    public static void serverTick(Level level, BlockPos pos, BlockState state, CPUCoreBlockEntity be) {
        be.scanCooldown--;
        if (be.scanCooldown <= 0) {
            be.scanCooldown = SCAN_INTERVAL;
            be.scanForRacks();
        }

        be.generateCycles();
    }

    private void generateCycles() {
        if (this.level == null) return;

        if(this.cycleStorage.getCyclesAvailable() >= this.cycleStorage.getCycleCapacity()) return;

        if(this.connectedRacks.isEmpty()) return;

        if (this.connectedRacks.size() > MAX_RACKS) return;

        int processorCount = 0;
        for (BlockPos rackPos : this.connectedRacks) {
            if (this.level.getBlockEntity(rackPos) instanceof ServerRackBlockEntity rack) {
                processorCount += rack.getProcessorCount();
            }
        }

        if(processorCount == 0) return;

        long cyclesToGenerate = BASE_CYCLE_GENERATION + ((long) processorCount * CYCLES_PER_PROCESSOR);
        long feCost = cyclesToGenerate * FE_PER_CYCLE;

        // Simulate energy extraction
        if (this.energyStorage.extractEnergy((int) feCost, true) == feCost) {
            // Real extraction
            this.energyStorage.extractEnergy((int) feCost, false);
            // Add cycles
            this.cycleStorage.receiveCycles(cyclesToGenerate, false);
            setChanged();
        }
    }

    private void scanForRacks() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        this.connectedRacks.clear();
        this.cablesInQueue.clear();

        scanLoop(this.worldPosition);

        if (this.level instanceof ServerLevel serverLevel) {
            NetworkManager.get(serverLevel).scanAt(serverLevel, this.worldPosition);
        }
        setChanged();
    }

    private void scanLoop(BlockPos scanPosition){
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = scanPosition.relative(direction);
            BlockEntity be = this.level.getBlockEntity(neighborPos);
            Block b = this.level.getBlockState(neighborPos).getBlock();

            if (be instanceof ServerRackBlockEntity rack) {
                if (!this.connectedRacks.contains(neighborPos)) {
                    rack.setControllerPos(this.worldPosition);
                    this.connectedRacks.add(neighborPos);
                }
            } else if (be == null && b instanceof ServerRackBlock) {
                be = this.level.getBlockEntity(neighborPos.below());
                if (be instanceof ServerRackBlockEntity rack) {
                    if (!this.connectedRacks.contains(neighborPos.below())) {
                        rack.setControllerPos(this.worldPosition);
                        this.connectedRacks.add(neighborPos.below());
                    }
                }
            }else if(b instanceof DataCableBlock){
                if(!this.cablesInQueue.contains(neighborPos)) {
                    this.cablesInQueue.add(neighborPos);
                    scanLoop(neighborPos);
                }
            }
        }
    }

    // --- NBT ---
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", this.energyStorage.serializeNBT(registries));
        tag.put("cycles", this.cycleStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy", 3)) {
            this.energyStorage.deserializeNBT(registries, tag.get("energy"));
        }
        if (tag.contains("cycles", 10)) {
            this.cycleStorage.deserializeNBT(registries, (CompoundTag) tag.get("cycles"));
        }
    }

    public void unlinkAllRacks() {
        if (this.level == null || this.level.isClientSide) return;
        this.connectedRacks.forEach(pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            Block block = level.getBlockState(pos).getBlock();
            if (be instanceof ServerRackBlockEntity rack) {
                rack.setControllerPos(null);
            } else if (be == null && block instanceof ServerRackBlock) {
                be = this.level.getBlockEntity(pos.below());
                if (be instanceof ServerRackBlockEntity rack) {
                    rack.setControllerPos(null);
                }
            }
        });
        this.connectedRacks.clear();
        this.cablesInQueue.clear();
        this.cycleStorage.setCycles(0L);
        setChanged();
    }
}
