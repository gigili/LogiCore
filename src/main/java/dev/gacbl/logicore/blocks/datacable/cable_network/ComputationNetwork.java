package dev.gacbl.logicore.blocks.datacable.cable_network;

import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.*;

public class ComputationNetwork {
    private static final int MAX_PULL_PER_SOURCE_PER_TICK = 1000;

    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> providers = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private final Set<BlockPos> energySources = new HashSet<>();
    private final EnergyStorage networkEnergyBuffer = new EnergyStorage(1_000_000, 10000, 10000);

    private UUID NETWORK_UUID = UUID.randomUUID();

    private boolean isDirty = true;
    private long cycleDemand = 0;
    private long feDemand = 0;
    private int energySourceIndex = 0;
    private int providerIndex = 0;
    private int consumerIndex = 0;


    public void merge(ComputationNetwork other) {
        if (other == null) return;

        this.cables.addAll(other.cables);
        this.providers.addAll(other.providers);
        this.consumers.addAll(other.consumers);
        this.energySources.addAll(other.energySources);
        this.networkEnergyBuffer.receiveEnergy(other.networkEnergyBuffer.getEnergyStored(), false);
        this.setDirty();
    }

    public void tick(Level level) {
        if (this.isDirty) {
            rebuild(level);
            this.isDirty = false;
        }

        if (this.networkEnergyBuffer.getEnergyStored() < this.networkEnergyBuffer.getMaxEnergyStored()) {
            pullEnergy(level);
        }

        if (this.feDemand > 0 && this.networkEnergyBuffer.getEnergyStored() > 0) {
            distributeFe(level);
        }

        if (this.cycleDemand > 0) {
            distributeCycles(level);
        }
    }

    public void addCable(BlockPos pos) {
        this.cables.add(pos);
    }

    public void addProvider(BlockPos pos) {
        this.providers.add(pos);
    }

    public void addConsumer(BlockPos pos) {
        this.consumers.add(pos);
    }

    public void addEnergySource(BlockPos pos) {
        this.energySources.add(pos);
    }

    public Set<BlockPos> getCables() {
        return this.cables;
    }

    public void scanDevice(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            // The device was removed, mark as dirty
            this.providers.remove(pos);
            this.consumers.remove(pos);
            this.energySources.remove(pos);
            this.setDirty();
            return;
        }

        boolean isProvider = level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null) != null;
        boolean isConsumer = level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null) != null;
        IEnergyStorage energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);

        if (isProvider) {
            this.providers.add(pos);
        } else {
            this.providers.remove(pos);
        }

        if (isConsumer) {
            this.consumers.add(pos);
        } else {
            this.consumers.remove(pos);
        }

        // Add as an energy source IF it can provide energy AND is NOT a provider/consumer
        if (!isProvider && !isConsumer && energy != null && energy.canExtract()) {
            this.energySources.add(pos);
        } else {
            this.energySources.remove(pos);
        }
    }

    private void rebuild(Level level) {
        this.cycleDemand = 0;
        this.feDemand = 0;

        // Clean up lists by removing dead devices
        this.providers.removeIf(pos -> !(level.getBlockEntity(pos) instanceof BlockEntity && level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null) != null));
        this.consumers.removeIf(pos -> !(level.getBlockEntity(pos) instanceof BlockEntity && level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null) != null));
        this.energySources.removeIf(pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return true;
            boolean isCycleDevice = level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null) != null || level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null) != null;
            IEnergyStorage energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
            return isCycleDevice || energy == null || !energy.canExtract();
        });


        for (BlockPos pos : this.providers) {
            IEnergyStorage energy = getEnergyConsumerAt(level, pos);
            if (energy != null) {
                this.feDemand += (energy.getMaxEnergyStored() - energy.getEnergyStored());
            }
        }

        for (BlockPos pos : this.consumers) {
            ICycleConsumer consumer = getConsumerAt(level, pos);
            if (consumer != null) {
                this.cycleDemand += consumer.getCycleDemand();
            }

            IEnergyStorage energy = getEnergyConsumerAt(level, pos);
            if (energy != null) {
                this.feDemand += (energy.getMaxEnergyStored() - energy.getEnergyStored());
            }
        }
    }

    private void pullEnergy(Level level) {
        if (this.energySources.isEmpty()) return;

        int energyNeeded = networkEnergyBuffer.getMaxEnergyStored() - networkEnergyBuffer.getEnergyStored();
        int sourcesToTry = this.energySources.size();
        List<BlockPos> sources = new ArrayList<>(this.energySources);

        for (int i = 0; i < sourcesToTry && energyNeeded > 0; i++) {
            // This increments the index and wraps it around if it reaches the end
            this.energySourceIndex = (this.energySourceIndex + 1) % sources.size();
            BlockPos sourcePos = sources.get(this.energySourceIndex);

            IEnergyStorage source = getEnergyProviderAt(level, sourcePos);
            if (source != null && source.canExtract()) {
                // Calculate how much to pull: min of (what the network needs, what this source can output, what this source has)
                int pullAmount = Math.min(energyNeeded, MAX_PULL_PER_SOURCE_PER_TICK);

                // Simulate extraction to see what we'd get
                int receivedSim = source.extractEnergy(pullAmount, true);
                if (receivedSim > 0) {
                    // Simulate receiving to see what buffer will take
                    int acceptedSim = this.networkEnergyBuffer.receiveEnergy(receivedSim, true);
                    if (acceptedSim > 0) {
                        // Perform the actual extraction and reception
                        int received = source.extractEnergy(acceptedSim, false);
                        int accepted = this.networkEnergyBuffer.receiveEnergy(received, false);
                        energyNeeded -= accepted;
                    }
                }
            }
        }
    }

    private void distributeFe(Level level) {
        if (this.providers.isEmpty() || this.feDemand <= 0) return;

        int energyToDistribute = this.networkEnergyBuffer.getEnergyStored();
        if (energyToDistribute <= 0) return;

        List<BlockPos> providers = new ArrayList<>(this.providers);
        int providersToTry = providers.size();

        for (int i = 0; i < providersToTry && energyToDistribute > 0; i++) {
            this.providerIndex = (this.providerIndex + 1) % providers.size();
            BlockPos providerPos = providers.get(this.providerIndex);

            // Use getEnergyConsumerAt to get the Server Rack's buffer
            IEnergyStorage providerEnergy = getEnergyConsumerAt(level, providerPos);
            if (providerEnergy != null && providerEnergy.canReceive()) {
                int energyNeeded = providerEnergy.getMaxEnergyStored() - providerEnergy.getEnergyStored();
                if (energyNeeded > 0) {
                    // Calculate how much to push: min of (what the network has, what this rack needs, max transfer rate)
                    int pushAmount = Math.min(Math.min(energyToDistribute, MAX_PULL_PER_SOURCE_PER_TICK), energyNeeded);

                    int accepted = providerEnergy.receiveEnergy(pushAmount, false);
                    if (accepted > 0) {
                        this.networkEnergyBuffer.extractEnergy(accepted, false);
                        energyToDistribute -= accepted;
                    }
                }
            }
        }
    }

    private void distributeCycles(Level level) {
        if (this.consumers.isEmpty() || this.providers.isEmpty()) return;

        List<BlockPos> consumers = new ArrayList<>(this.consumers);
        List<BlockPos> providers = new ArrayList<>(this.providers);

        // Try one consumer per tick (round-robin)
        this.consumerIndex = (this.consumerIndex + 1) % consumers.size();
        BlockPos consumerPos = consumers.get(this.consumerIndex);
        ICycleConsumer consumer = getConsumerAt(level, consumerPos);

        if (consumer == null) return;
        long cyclesNeeded = consumer.getCycleDemand();
        if (cyclesNeeded <= 0) return;

        // Try all providers (round-robin) to fill that one consumer
        int providersToTry = providers.size();
        for (int i = 0; i < providersToTry && cyclesNeeded > 0; i++) {
            this.providerIndex = (this.providerIndex + 1) % providers.size();
            BlockPos providerPos = providers.get(this.providerIndex);

            ICycleProvider provider = getProviderAt(level, providerPos);
            if(provider != null) {
                long extracted = provider.extractCycles(cyclesNeeded, false);
                if (extracted > 0) {
                    long accepted = consumer.receiveCycles(extracted, false);
                    cyclesNeeded -= accepted;

                    // If not all was accepted, put it back
                    if (accepted < extracted) {
                        provider.receiveCycles(extracted - accepted, false);
                    }
                }
            }
        }
    }

    private ICycleProvider getProviderAt(Level level, BlockPos pos) {
        return level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null);
    }

    // Check if a block is a provider OR consumer
    private boolean isProviderOrConsumer(Level level, BlockPos pos) {
        var capCyclesP = level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null);
        var capCyclesC = level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null);
        return capCyclesP != null || capCyclesC != null;
    }

    // Gets energy from a block IF it is NOT a cycle device (e.g., generator)
    private IEnergyStorage getEnergyProviderAt(Level level, BlockPos pos) {
        var cap = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (cap == null) return null;

        if (!isProviderOrConsumer(level, pos)) {
            return cap;
        }
        return null;
    }

    // Gets energy from a block IF it IS a cycle device (e.g., server rack)
    private IEnergyStorage getEnergyConsumerAt(Level level, BlockPos pos) {
        var cap = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (cap == null) return null;

        if (isProviderOrConsumer(level, pos)) {
            return cap;
        }
        return null;
    }

    private ICycleConsumer getConsumerAt(Level level, BlockPos pos) {
        return level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null);
    }

    public Set<BlockPos> getProviders() {
        return this.providers;
    }

    public Set<BlockPos> getConsumers() {
        return this.consumers;
    }

    public Set<BlockPos> getEnergySources() {
        return this.energySources;
    }

    public UUID getNetworkID() {
        return NETWORK_UUID;
    }

    public void setNetworkID(UUID networkUUID) {
        NETWORK_UUID = networkUUID;
        this.setDirty();
    }

    public void clearAll() {
        this.consumers.clear();
        this.providers.clear();
        this.energySources.clear();
        this.cables.clear();
        this.setDirty();
    }

    public void setDirty() {
        this.isDirty = true;
    }
}
