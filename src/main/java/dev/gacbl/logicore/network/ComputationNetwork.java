package dev.gacbl.logicore.network;

import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.HashSet;
import java.util.Set;

public class ComputationNetwork {
    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> providers = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private boolean isDirty = true;
    private long cycleCapacity = 0;
    private long cycleDemand = 0;

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

    public Set<BlockPos> getCables() {
        return this.cables;
    }

    public void merge(ComputationNetwork other) {
        this.cables.addAll(other.cables);
        this.providers.addAll(other.providers);
        this.consumers.addAll(other.consumers);
        this.isDirty = true;
    }

    public void tick(Level level) {
        if (this.isDirty) {
            recalculateStats(level);
            this.isDirty = false;
        }

        if (this.cycleDemand == 0 || this.cycleCapacity == 0) {
            return;
        }

        distributeCycles(level);
    }

    private void recalculateStats(Level level) {
        this.cycleCapacity = 0;
        this.cycleDemand = 0;

        for (BlockPos pos : this.providers) {
            ICycleProvider provider = getProviderAt(level, pos);
            if (provider != null) {
                this.cycleCapacity += provider.getCyclesAvailable();
            }
        }

        for (BlockPos pos : this.consumers) {
            ICycleConsumer consumer = getConsumerAt(level, pos);
            if (consumer != null) {
                this.cycleDemand += consumer.getCycleDemand();
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

    private ICycleProvider getProviderAt(Level level, BlockPos pos) {
        return level.getCapability(dev.gacbl.logicore.core.ModCapabilities.CYCLE_STORAGE, pos, null);
    }

    private ICycleConsumer getConsumerAt(Level level, BlockPos pos) {
        return level.getCapability(dev.gacbl.logicore.core.ModCapabilities.CYCLE_STORAGE, pos, null);
    }

    public Set<BlockPos> getProviders() {
        return this.providers;
    }

    public Set<BlockPos> getConsumers() {
        return this.consumers;
    }
}
