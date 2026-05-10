package dev.gacbl.logicore.api.computation;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    @Override
    public long getCyclesStored() {
        return this.cycles;
    }

    public void setCycleDemand(long cycleDemand) {
        this.cycleDemand = cycleDemand;
    }

    public void setCycles(long cycles) {
        this.cycles = Math.max(0, Math.min(this.capacity, cycles));
    }

    public void serialize(ValueOutput output) {
        output.store("cycles", Codec.LONG, this.cycles);
        output.store("capacity", Codec.LONG, this.capacity);
        output.store("maxReceive", Codec.LONG, this.maxReceive);
        output.store("maxExtract", Codec.LONG, this.maxExtract);
    }

    public void deserialize(ValueInput input) {
        this.cycles = input.read("cycles", Codec.LONG).orElse(0L);
        this.capacity = input.read("capacity", Codec.LONG).orElse(this.capacity);
        this.maxReceive = input.read("maxReceive", Codec.LONG).orElse(this.maxReceive);
        this.maxExtract = input.read("maxExtract", Codec.LONG).orElse(this.maxExtract);
        if (this.cycles > this.capacity) {
            this.cycles = this.capacity;
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("cycles", this.cycles);
        tag.putLong("capacity", this.capacity);
        tag.putLong("maxReceive", this.maxReceive);
        tag.putLong("maxExtract", this.maxExtract);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        this.cycles = nbt.getLong("cycles").orElse(0L);
        this.capacity = nbt.contains("capacity") ? nbt.getLong("capacity").orElse(this.capacity) : this.capacity;
        this.maxReceive = nbt.contains("maxReceive") ? nbt.getLong("maxReceive").orElse(this.maxReceive) : this.maxReceive;
        this.maxExtract = nbt.contains("maxExtract") ? nbt.getLong("maxExtract").orElse(this.maxExtract) : this.maxExtract;
        if (this.cycles > this.capacity) {
            this.cycles = this.capacity;
        }
    }
}
