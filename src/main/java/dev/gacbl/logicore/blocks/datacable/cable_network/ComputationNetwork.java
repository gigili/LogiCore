package dev.gacbl.logicore.blocks.datacable.cable_network;

import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.*;

public class ComputationNetwork {
    private static final int MAX_PULL_PER_SOURCE_PER_TICK = 1_000_000;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> providers = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private final Set<BlockPos> energySources = new HashSet<>();
    private final SimpleEnergyHandler networkEnergyBuffer = new SimpleEnergyHandler(MAX_PULL_PER_SOURCE_PER_TICK * 3, MAX_PULL_PER_SOURCE_PER_TICK * 2, MAX_PULL_PER_SOURCE_PER_TICK * 2);

    private static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

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
        int transferableEnergy = (int) Math.min(Integer.MAX_VALUE, other.networkEnergyBuffer.getAmountAsLong());
        insertIntoBuffer(transferableEnergy);
        this.setDirty();
    }

    public void tick(Level level) {
        if (this.isDirty) {
            rebuild(level);
            this.isDirty = false;
        }

        this.cycleDemand = calculateCycleDemand(level);
        this.feDemand = calculateFeDemand(level);

        if (this.networkEnergyBuffer.getAmountAsLong() < this.networkEnergyBuffer.getCapacityAsLong() && this.feDemand > 0) {
            pullEnergy(level, this.feDemand);
        }

        if (this.networkEnergyBuffer.getAmountAsLong() > 0) {
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
        BlockPos normalizedPos = normalizeDevicePos(level, pos);
        this.providers.remove(normalizedPos);
        this.consumers.remove(normalizedPos);
        this.energySources.remove(normalizedPos);
        classifyDevice(level, normalizedPos);
    }

    private void rebuild(Level level) {
        this.cycleDemand = 0;
        this.feDemand = 0;

        this.cables.removeIf(pos -> !(level.getBlockState(pos).getBlock() instanceof DataCableBlock));

        this.providers.clear();
        this.consumers.clear();
        this.energySources.clear();

        Set<BlockPos> checkedDevices = new HashSet<>();
        for (BlockPos cablePos : this.cables) {
            for (Direction direction : DIRECTIONS) {
                BlockPos neighborPos = cablePos.relative(direction);
                if (level.getBlockState(neighborPos).getBlock() instanceof DataCableBlock) {
                    continue;
                }
                if (checkedDevices.add(neighborPos)) {
                    classifyDevice(level, neighborPos);
                }
            }
        }

        this.cycleDemand = calculateCycleDemand(level);
        this.feDemand = calculateFeDemand(level);
    }

    public void requestCycles(long cycleDemand) {
        this.cycleDemand += cycleDemand;
    }

    public long extractCycles(ServerLevel level, long cyclesNeeded) {
        return this.extractCycles(level, cyclesNeeded, null);
    }

    public long extractCycles(ServerLevel level, long cyclesNeeded, @Nullable BlockPos providerToIgnore) {
        List<BlockPos> providers = new ArrayList<>(this.providers);
        if (providers.isEmpty() || cyclesNeeded <= 0) {
            return 0;
        }

        long extractedTotal = 0;

        int providersToTry = providers.size();
        for (int i = 0; i < providersToTry && cyclesNeeded > 0; i++) {
            this.providerIndex = (this.providerIndex + 1) % providers.size();
            BlockPos providerPos = providers.get(this.providerIndex);

            if (providerPos.equals(providerToIgnore)) continue;

            ICycleProvider provider = getProviderAt(level, providerPos);
            if (provider != null) {
                long extracted = provider.extractCycles(cyclesNeeded, false);
                if (extracted > 0) {
                    extractedTotal += extracted;
                    cyclesNeeded -= extracted;
                }
            }
        }
        return extractedTotal;
    }

    private void pullEnergy(Level level, long feDemand) {
        if (this.energySources.isEmpty() || feDemand <= 0) return;

        int energyNeeded = (int) Math.min(
                Math.min(feDemand, Integer.MAX_VALUE),
                this.networkEnergyBuffer.getCapacityAsLong() - this.networkEnergyBuffer.getAmountAsLong()
        );
        int sourcesToTry = this.energySources.size();
        List<BlockPos> sources = new ArrayList<>(this.energySources);

        for (int i = 0; i < sourcesToTry && energyNeeded > 0; i++) {
            // This increments the index and wraps it around if it reaches the end
            this.energySourceIndex = (this.energySourceIndex + 1) % sources.size();
            BlockPos sourcePos = sources.get(this.energySourceIndex);

            EnergyHandler source = getEnergyProviderAt(level, sourcePos);
            if (source != null && source.getAmountAsLong() > 0) {
                // Calculate how much to pull: min of (what the network needs, what this source can output, what this source has)
                int pullAmount = Math.min(energyNeeded, MAX_PULL_PER_SOURCE_PER_TICK);

                // Simulate extraction to see what we'd get
                int receivedSim;
                try (Transaction tx = Transaction.openRoot()) {
                    receivedSim = source.extract(pullAmount, tx);
                }
                if (receivedSim > 0) {
                    // Simulate receiving to see what the buffer will take
                    int acceptedSim = simulateInsertIntoBuffer(receivedSim);
                    if (acceptedSim > 0) {
                        // Perform the actual extraction and reception
                        int received;
                        try (Transaction tx = Transaction.openRoot()) {
                            received = source.extract(acceptedSim, tx);
                            tx.commit();
                        }
                        int accepted = insertIntoBuffer(received);
                        energyNeeded -= accepted;
                    }
                }
            }
        }
    }

    private void distributeFe(Level level) {
        if (this.providers.isEmpty() && this.consumers.isEmpty()) return;

        int energyToDistribute = (int) Math.min(this.networkEnergyBuffer.getAmountAsLong(), Integer.MAX_VALUE);
        if (energyToDistribute <= 0) return;

        Set<BlockPos> energyTargets = new LinkedHashSet<>(this.providers);
        energyTargets.addAll(this.consumers);
        if (energyTargets.isEmpty()) {
            return;
        }

        List<BlockPos> targets = new ArrayList<>(energyTargets);
        int providersToTry = targets.size();

        for (int i = 0; i < providersToTry && energyToDistribute > 0; i++) {
            this.providerIndex = (this.providerIndex + 1) % targets.size();
            BlockPos providerPos = targets.get(this.providerIndex);

            EnergyHandler providerEnergy = getEnergyConsumerAt(level, providerPos);
            if (providerEnergy != null && providerEnergy.getAmountAsLong() < providerEnergy.getCapacityAsLong()) {
                long neededLong = providerEnergy.getCapacityAsLong() - providerEnergy.getAmountAsLong();
                int energyNeeded = (int) Math.min(neededLong, Integer.MAX_VALUE);
                if (energyNeeded > 0) {
                    // Calculate how much to push: min of (what the network has, what this rack needs, max transfer rate)
                    int pushAmount = Math.min(Math.min(energyToDistribute, MAX_PULL_PER_SOURCE_PER_TICK), energyNeeded);

                    int accepted;
                    try (Transaction tx = Transaction.openRoot()) {
                        accepted = providerEnergy.insert(pushAmount, tx);
                        tx.commit();
                    }
                    if (accepted > 0) {
                        extractFromBuffer(accepted);
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
            if (provider != null) {
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
        ICycleProvider provider = level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, null);
        if (provider != null) {
            return provider;
        }

        for (Direction direction : DIRECTIONS) {
            provider = level.getCapability(ModCapabilities.CYCLE_PROVIDER, pos, direction);
            if (provider != null) {
                return provider;
            }
        }
        return null;
    }

    private boolean isProviderOrConsumer(Level level, BlockPos pos) {
        var capCyclesP = getProviderAt(level, pos);
        var capCyclesC = getConsumerAt(level, pos);
        return capCyclesP != null || capCyclesC != null;
    }

    private EnergyHandler getEnergyCapability(Level level, BlockPos pos) {
        EnergyHandler cap = level.getCapability(Capabilities.Energy.BLOCK, pos, null);
        if (cap != null) {
            return cap;
        }

        for (Direction direction : DIRECTIONS) {
            cap = level.getCapability(Capabilities.Energy.BLOCK, pos, direction);
            if (cap != null) {
                return cap;
            }
        }

        return null;
    }

    private EnergyHandler getEnergyProviderAt(Level level, BlockPos pos) {
        var cap = getEnergyCapability(level, pos);
        if (cap == null) return null;

        if (!isProviderOrConsumer(level, pos)) {
            return cap;
        }
        return null;
    }

    private EnergyHandler getEnergyConsumerAt(Level level, BlockPos pos) {
        var cap = getEnergyCapability(level, pos);
        if (cap == null) return null;

        if (isProviderOrConsumer(level, pos)) {
            return cap;
        }
        return null;
    }

    private ICycleConsumer getConsumerAt(Level level, BlockPos pos) {
        ICycleConsumer consumer = level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, null);
        if (consumer != null) {
            return consumer;
        }

        for (Direction direction : DIRECTIONS) {
            consumer = level.getCapability(ModCapabilities.CYCLE_CONSUMER, pos, direction);
            if (consumer != null) {
                return consumer;
            }
        }
        return null;
    }

    private BlockPos normalizeDevicePos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(HALF) && state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return pos.below();
        }
        return pos;
    }

    private void classifyDevice(Level level, BlockPos rawPos) {
        BlockPos pos = normalizeDevicePos(level, rawPos);
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return;
        }

        boolean isProvider = getProviderAt(level, pos) != null;
        boolean isConsumer = getConsumerAt(level, pos) != null;
        EnergyHandler energy = getEnergyCapability(level, pos);

        if (isProvider) {
            this.providers.add(pos);
        }

        if (isConsumer) {
            this.consumers.add(pos);
        }

        if (!isProvider && !isConsumer && energy != null && energy.getCapacityAsLong() > 0) {
            this.energySources.add(pos);
        }
    }

    private long calculateCycleDemand(Level level) {
        long demand = 0;
        for (BlockPos pos : this.consumers) {
            ICycleConsumer consumer = getConsumerAt(level, pos);
            if (consumer != null) {
                demand += Math.max(0, consumer.getCycleDemand());
            }
        }
        return demand;
    }

    private long calculateFeDemand(Level level) {
        long demand = 0;

        for (BlockPos pos : this.providers) {
            EnergyHandler energy = getEnergyConsumerAt(level, pos);
            if (energy != null) {
                demand += Math.max(0, energy.getCapacityAsLong() - energy.getAmountAsLong());
            }
        }

        for (BlockPos pos : this.consumers) {
            EnergyHandler energy = getEnergyConsumerAt(level, pos);
            if (energy != null) {
                demand += Math.max(0, energy.getCapacityAsLong() - energy.getAmountAsLong());
            }
        }

        return demand;
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

    private int simulateInsertIntoBuffer(int amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            return this.networkEnergyBuffer.insert(amount, tx);
        }
    }

    private int insertIntoBuffer(int amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = this.networkEnergyBuffer.insert(amount, tx);
            tx.commit();
            return inserted;
        }
    }

    private int extractFromBuffer(int amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = this.networkEnergyBuffer.extract(amount, tx);
            tx.commit();
            return extracted;
        }
    }

    public void setDirty() {
        this.isDirty = true;
    }
}
