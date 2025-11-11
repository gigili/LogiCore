package dev.gacbl.logicore.api.computation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class CycleStorage implements ICycleStorage {
    protected long capacity;
    protected long cycles;
    protected long maxReceive;
    protected long maxExtract;
    protected long cycleDemand;

    public CycleStorage(long capacity) {
        this(capacity, capacity, capacity, 0, 0);
    }

    public CycleStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0, 0);
    }

    public CycleStorage(long capacity, long maxReceive, long maxExtract) {
        this(capacity, maxReceive, maxExtract, 0, 0);
    }

    public CycleStorage(long capacity, long maxReceive, long maxExtract, long cycles, long cycleDemand) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.cycles = Math.max(0, Math.min(capacity, cycles));
        this.cycleDemand = cycleDemand;
    }

    @Override
    public long getCyclesAvailable() {
        return this.cycles;
    }

    @Override
    public long getCycleCapacity() {
        return this.capacity;
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        long cyclesExtracted = Math.min(this.cycles, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            this.cycles -= cyclesExtracted;
        }
        return cyclesExtracted;
    }

    @Override
    public long getCycleDemand() {
        return this.cycleDemand;
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        long cyclesReceived = Math.min(this.capacity - this.cycles, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            this.cycles += cyclesReceived;
        }
        return cyclesReceived;
    }

    public void setCycleDemand(long cycleDemand) {
        this.cycleDemand = cycleDemand;
    }

    public void setCycles(long cycles) {
        this.cycles = Math.max(0, Math.min(this.capacity, cycles));
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Cycles", this.cycles);
        tag.putLong("Capacity", this.capacity);
        tag.putLong("MaxReceive", this.maxReceive);
        tag.putLong("MaxExtract", this.maxExtract);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        this.cycles = nbt.getLong("Cycles");
        this.capacity = nbt.contains("Capacity") ? nbt.getLong("Capacity") : this.capacity;
        this.maxReceive = nbt.contains("MaxReceive") ? nbt.getLong("MaxReceive") : this.maxReceive;
        this.maxExtract = nbt.contains("MaxExtract") ? nbt.getLong("MaxExtract") : this.maxExtract;

        if (this.cycles > this.capacity) {
            this.cycles = this.capacity;
        }
    }
}
