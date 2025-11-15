package dev.gacbl.logicore.blocks.datacable.network;

import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ComputationNetwork {
    private static final int MAX_PULL_PER_SOURCE_PER_TICK = 1000;

    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> providers = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private final Set<BlockPos> energySources = new HashSet<>();
    private final EnergyStorage networkEnergyBuffer = new EnergyStorage(1_000_000, 10000, MAX_PULL_PER_SOURCE_PER_TICK);

    private final String networkUUID = UUID.randomUUID().toString();

    private boolean isDirty = true;
    private long cycleCapacity = 0;
    private long cycleDemand = 0;
    private long feDemand = 0;

    public void merge(ComputationNetwork other) {
        this.cables.addAll(other.cables);
        this.providers.addAll(other.providers);
        this.consumers.addAll(other.consumers);
        this.energySources.addAll(other.energySources);
        this.isDirty = true;
    }

    public void addCable(BlockPos pos) {
        if (this.cables.add(pos)) {
            this.isDirty = true;
        }
    }

    public void addProvider(BlockPos pos) {
        if (this.providers.add(pos)) {
            this.isDirty = true;
        }
    }

    public void addConsumer(BlockPos pos) {
        if (this.consumers.add(pos)) {
            this.isDirty = true;
        }
    }

    public void addEnergySource(BlockPos pos) {
        if (this.energySources.add(pos)) {
            this.isDirty = true;
        }
    }

    public Set<BlockPos> getCables() {
        return this.cables;
    }

    public void tick(Level level) {
        recalculateStats(level);

        if (this.cycleDemand > 0 && this.cycleCapacity > 0) {
            distributeCycles(level);
        }

        if (this.feDemand > 0) {
            distributeFe(level);
        }
    }

    private void recalculateStats(Level level) {
        this.cycleCapacity = 0;
        this.cycleDemand = 0;

        this.feDemand = 0;

        for (BlockPos pos : this.providers) {
            ICycleProvider provider = getProviderAt(level, pos);
            if (provider != null) {
                this.cycleCapacity += provider.getCyclesAvailable();
            }

            IEnergyStorage feProvider = getEnergyConsumerAt(level, pos);
            if (feProvider != null) {
                this.feDemand += feProvider.getMaxEnergyStored() - feProvider.getEnergyStored();
            }
        }

        for (BlockPos pos : this.consumers) {
            ICycleConsumer consumer = getConsumerAt(level, pos);
            if (consumer != null) {
                this.cycleDemand += consumer.getCycleDemand();
            }

            IEnergyStorage feProvider = getEnergyProviderAt(level, pos);
            if (feProvider != null) {
                this.feDemand += feProvider.getMaxEnergyStored() - feProvider.getEnergyStored();
            }
        }


        if (this.feDemand > 0) {
            for (BlockPos pos : this.energySources) {
                IEnergyStorage provider = getEnergyProviderAt(level, pos);
                if (provider != null) {
                    this.networkEnergyBuffer.receiveEnergy(MAX_PULL_PER_SOURCE_PER_TICK, false);
                    provider.extractEnergy(MAX_PULL_PER_SOURCE_PER_TICK, false);
                }
            }
        }
    }

    private void distributeCycles(Level level) {
        if (this.cycleDemand == 0 || this.cycleCapacity == 0) return;

        double efficiency = Math.min(1.0, (double) this.cycleCapacity / (double) this.cycleDemand);

        for (BlockPos consumerPos : this.consumers) {
            ICycleConsumer consumer = getConsumerAt(level, consumerPos);
            if (consumer == null) continue;

            long demand = consumer.getCycleDemand();
            if (demand == 0) continue;

            long cyclesToProvide = (long) (demand * efficiency);
            if (cyclesToProvide == 0 && demand > 0) continue;

            long accepted = 0;
            for (BlockPos providerPos : this.providers) {
                ICycleProvider provider = getProviderAt(level, providerPos);
                if (provider == null) continue;

                long cyclesExtracted = provider.extractCycles(cyclesToProvide - accepted, false);
                accepted += consumer.receiveCycles(cyclesExtracted, false);

                if (accepted >= cyclesToProvide) {
                    break;
                }
            }
        }
    }

    private void distributeFe(Level level) {
        if (this.feDemand == 0) return;

        Set<BlockPos> energyRecipients = new HashSet<>();
        energyRecipients.addAll(this.consumers);
        energyRecipients.addAll(this.providers);

        for (BlockPos pos : energyRecipients) {
            IEnergyStorage provider = getEnergyProviderAt(level, pos);
            if (provider != null) {
                int pull = MAX_PULL_PER_SOURCE_PER_TICK;
                provider.receiveEnergy(pull, false);
                this.networkEnergyBuffer.extractEnergy(pull, false);
            }
        }
    }

    private ICycleProvider getProviderAt(Level level, BlockPos pos) {
        return level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null);
    }

    private boolean isEnergySource(Level level, BlockPos pos) {
        var capCycles = isProviderOrConsumer(level, pos);
        var capFe = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        return !capCycles && capFe != null;
    }

    private boolean isProviderOrConsumer(Level level, BlockPos pos) {
        return isProvider(level, pos) || isConsumer(level, pos);
    }

    private boolean isConsumer(Level level, BlockPos pos) {
        var capCycles = level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null);
        var capFe = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        return capCycles != null && capFe != null;
    }

    private boolean isProvider(Level level, BlockPos pos) {
        var capCycles = level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null);
        var capFe = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        return capCycles != null && capFe != null;
    }

    private IEnergyStorage getEnergyProviderAt(Level level, BlockPos pos) {
        var cap = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        var cycleCap = isProviderOrConsumer(level, pos);

        if (!cycleCap && cap != null) {
            return cap;
        }

        return null;
    }

    private IEnergyStorage getEnergyConsumerAt(Level level, BlockPos pos) {
        var cap = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        var cycleCap = isProviderOrConsumer(level, pos);

        if (cycleCap && cap != null) {
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

    public String getNetworkUUID() {
        return networkUUID;
    }

    public void clearAll() {
        this.consumers.clear();
        this.providers.clear();
        this.energySources.clear();
    }
}
