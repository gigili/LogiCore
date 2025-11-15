package dev.gacbl.logicore.blocks.datacable.network;

import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NetworkManager extends SavedData {
    private static final String NAME = "logicore_network_manager";
    private static final String POS_KEY = "pos";

    private final Map<BlockPos, ComputationNetwork> networksByCable = new HashMap<>();
    private final Set<ComputationNetwork> networks = new HashSet<>();
    private final Map<String, List<BlockPos>> visitedNodes = new HashMap<>();

    public void tick(ServerLevel level) {
        for (ComputationNetwork network : this.networks) {
            network.tick(level);
        }
    }

    public void onCablePlaced(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        scanAt(level, pos);
    }

    public void onCableRemoved(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        ComputationNetwork oldNetwork = this.networksByCable.get(pos);
        if (oldNetwork == null) return;

        oldNetwork.clearAll();
        this.networks.remove(oldNetwork);
        this.visitedNodes.remove(oldNetwork.getNetworkUUID());

        Set<BlockPos> allBlocksInOldNetwork = new HashSet<>();
        allBlocksInOldNetwork.addAll(oldNetwork.getCables());
        allBlocksInOldNetwork.addAll(oldNetwork.getProviders());
        allBlocksInOldNetwork.addAll(oldNetwork.getConsumers());
        allBlocksInOldNetwork.addAll(oldNetwork.getEnergySources());

        for (BlockPos blockPos : allBlocksInOldNetwork) {
            this.networksByCable.remove(blockPos);
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if (allBlocksInOldNetwork.contains(neighborPos) && isCable(level.getBlockState(neighborPos).getBlock())) {
                scanAt(level, neighborPos);
            }
        }
    }

    public void scanAt(Level level, BlockPos startPos) {
        if (level.isClientSide || this.networksByCable.containsKey(startPos)) {
            return;
        }

        BlockState startState = level.getBlockState(startPos);
        if (!isNetworkComponent(level, startPos, startState)) {
            return;
        }

        ComputationNetwork newNetwork = new ComputationNetwork();
        Queue<BlockPos> toScan = new LinkedList<>();
        toScan.add(startPos);

        visitedNodes.computeIfAbsent(newNetwork.getNetworkUUID(), (k) -> new ArrayList<>());
        visitedNodes.get(newNetwork.getNetworkUUID()).clear();

        while (!toScan.isEmpty()) {
            BlockPos currentPos = toScan.poll();
            if (currentPos == null) break;

            if (this.networksByCable.containsKey(currentPos)) {
                ComputationNetwork existing = this.networksByCable.get(currentPos);
                if (existing != newNetwork) {
                    newNetwork.merge(existing);
                    for (BlockPos oldCablePos : existing.getCables()) {
                        this.networksByCable.remove(oldCablePos);
                    }
                    existing.clearAll();
                    this.networks.remove(existing);
                }
            }

            BlockState currentState = level.getBlockState(currentPos);
            if (!isNetworkComponent(level, currentPos, currentState)) {
                continue;
            }

            if (isCable(currentState.getBlock())) {
                newNetwork.addCable(currentPos);
            }

            if (isProvider(level, currentPos)) {
                newNetwork.addProvider(currentPos);
            }

            if (isConsumer(level, currentPos)) {
                newNetwork.addConsumer(currentPos);
            }

            if (isEnergySource(level, currentPos)) {
                newNetwork.addEnergySource(currentPos);
            }

            this.networksByCable.put(currentPos, newNetwork);

            if (isCable(currentState.getBlock())) {
                for (Direction dir : Direction.values()) {
                    if (!visitedNodes.get(newNetwork.getNetworkUUID()).contains(currentPos.relative(dir)) && isNetworkComponent(level, currentPos.relative(dir), level.getBlockState(currentPos.relative(dir)))) {
                        visitedNodes.get(newNetwork.getNetworkUUID()).add(currentPos.relative(dir));
                        toScan.add(currentPos.relative(dir));
                    }
                }
            }
        }

        this.networks.add(newNetwork);
        setDirty();
    }

    private boolean isNetworkComponent(Level level, BlockPos pos, BlockState state) {
        return isCable(state.getBlock()) || isProviderOrConsumer(level, pos) || isEnergySource(level, pos);
    }

    private boolean isCable(Block block) {
        return block instanceof DataCableBlock;
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

    private boolean isEnergySource(Level level, BlockPos pos) {
        var capCycles = isProviderOrConsumer(level, pos);
        var capFe = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        return !capCycles && capFe != null;
    }

    public static NetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(NetworkManager::new, NetworkManager::load, null), NAME);
    }

    public static NetworkManager load(CompoundTag tag, HolderLookup.Provider registries) {
        NetworkManager manager = new NetworkManager();
        ListTag networksTag = tag.getList("Networks", 10);
        for (int i = 0; i < networksTag.size(); i++) {
            CompoundTag networkTag = networksTag.getCompound(i);
            ComputationNetwork network = new ComputationNetwork();

            ListTag cablesTag = networkTag.getList("Cables", 10);
            for (int j = 0; j < cablesTag.size(); j++) {
                Optional<BlockPos> posOpt = NbtUtils.readBlockPos(cablesTag.getCompound(j), POS_KEY);
                posOpt.ifPresent(pos -> {
                    network.addCable(pos);
                    manager.networksByCable.put(pos, network);
                });
            }

            ListTag providersTag = networkTag.getList("Providers", 10);
            for (int j = 0; j < providersTag.size(); j++) {
                Optional<BlockPos> posOpt = NbtUtils.readBlockPos(providersTag.getCompound(j), POS_KEY);
                posOpt.ifPresent(pos -> {
                    network.addProvider(pos);
                    manager.networksByCable.put(pos, network);
                });
            }

            ListTag consumersTag = networkTag.getList("Consumers", 10);
            for (int j = 0; j < consumersTag.size(); j++) {
                Optional<BlockPos> posOpt = NbtUtils.readBlockPos(consumersTag.getCompound(j), POS_KEY);
                posOpt.ifPresent(pos -> {
                    network.addConsumer(pos);
                    manager.networksByCable.put(pos, network);
                });
            }

            ListTag energyTag = networkTag.getList("EnergySources", 10);
            for (int j = 0; j < energyTag.size(); j++) {
                Optional<BlockPos> posOpt = NbtUtils.readBlockPos(consumersTag.getCompound(j), POS_KEY);
                posOpt.ifPresent(pos -> {
                    network.addEnergySource(pos);
                    manager.networksByCable.put(pos, network);
                });
            }
            manager.networks.add(network);
        }
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag networksTag = new ListTag();
        for (ComputationNetwork network : this.networks) {
            CompoundTag networkTag = new CompoundTag();

            ListTag cablesTag = new ListTag();
            network.getCables().forEach(pos -> {
                CompoundTag posTag = new CompoundTag();
                posTag.put(POS_KEY, NbtUtils.writeBlockPos(pos));
                cablesTag.add(posTag);
            });
            networkTag.put("Cables", cablesTag);

            ListTag providersTag = new ListTag();
            network.getProviders().forEach(pos -> {
                CompoundTag posTag = new CompoundTag();
                posTag.put(POS_KEY, NbtUtils.writeBlockPos(pos));
                providersTag.add(posTag);
            });
            networkTag.put("Providers", providersTag);

            ListTag consumersTag = new ListTag();
            network.getConsumers().forEach(pos -> {
                CompoundTag posTag = new CompoundTag();
                posTag.put(POS_KEY, NbtUtils.writeBlockPos(pos));
                consumersTag.add(posTag);
            });
            networkTag.put("Consumers", consumersTag);

            ListTag energyTag = new ListTag();
            network.getEnergySources().forEach(pos -> {
                CompoundTag posTag = new CompoundTag();
                posTag.put(POS_KEY, NbtUtils.writeBlockPos(pos));
                energyTag.add(posTag);
            });
            networkTag.put("EnergySources", energyTag);

            networksTag.add(networkTag);
        }
        tag.put("Networks", networksTag);
        return tag;
    }

    public void clearAll() {
        this.networksByCable.clear();
        this.networks.clear();
        this.visitedNodes.clear();
        setDirty();
    }
}
