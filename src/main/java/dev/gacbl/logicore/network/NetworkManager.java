package dev.gacbl.logicore.network;

import dev.gacbl.logicore.datacable.DataCableBlock;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NetworkManager extends SavedData {
    private static final String NAME = "logicore_network_manager";
    private static final String POS_KEY = "pos";

    private final Map<BlockPos, ComputationNetwork> networksByCable = new HashMap<>();
    private final Set<ComputationNetwork> networks = new HashSet<>();

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

        this.networks.remove(oldNetwork);

        Set<BlockPos> allBlocksInOldNetwork = new HashSet<>();
        allBlocksInOldNetwork.addAll(oldNetwork.getCables());
        allBlocksInOldNetwork.addAll(oldNetwork.getProviders());
        allBlocksInOldNetwork.addAll(oldNetwork.getConsumers());

        for (BlockPos blockPos : allBlocksInOldNetwork) {
            this.networksByCable.remove(blockPos);
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if (allBlocksInOldNetwork.contains(neighborPos)) {
                scanAt(level, neighborPos);
            }
        }
    }

    public void scanAt(Level level, BlockPos startPos) {
        if (level.isClientSide || this.networksByCable.containsKey(startPos)) {
            return;
        }

        return;

        /*BlockState startState = level.getBlockState(startPos);
        if (!isNetworkComponent(level, startPos, startState)) {
            return;
        }

        ComputationNetwork newNetwork = new ComputationNetwork();
        Queue<BlockPos> toScan = new LinkedList<>();
        toScan.add(startPos);

        while (!toScan.isEmpty()) {
            BlockPos currentPos = toScan.poll();
            if (this.networksByCable.containsKey(currentPos)) {
                ComputationNetwork existing = this.networksByCable.get(currentPos);
                if (existing != newNetwork) {
                    newNetwork.merge(existing);
                    for (BlockPos oldCablePos : existing.getCables()) {
                        this.networksByCable.remove(oldCablePos);
                    }
                    this.networks.remove(existing);
                }
                continue;
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

            this.networksByCable.put(currentPos, newNetwork);

            if (isCable(currentState.getBlock())) {
                for (Direction dir : Direction.values()) {
                    toScan.add(currentPos.relative(dir));
                }
            }
        }

        this.networks.add(newNetwork);
        setDirty();*/
    }

    private boolean isNetworkComponent(Level level, BlockPos pos, BlockState state) {
        return isCable(state.getBlock()) || isProvider(level, pos) || isConsumer(level, pos);
    }

    private boolean isCable(Block block) {
        return block instanceof DataCableBlock;
    }

    private boolean isProvider(Level level, BlockPos pos) {
        var cap = level.getCapability(dev.gacbl.logicore.core.ModCapabilities.CYCLE_STORAGE, pos, null);
        return cap != null && cap.getCycleCapacity() > 0;
    }

    private boolean isConsumer(Level level, BlockPos pos) {
        var cap = level.getCapability(dev.gacbl.logicore.core.ModCapabilities.CYCLE_STORAGE, pos, null);
        return cap != null;
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

            networksTag.add(networkTag);
        }
        tag.put("Networks", networksTag);
        return tag;
    }
}
