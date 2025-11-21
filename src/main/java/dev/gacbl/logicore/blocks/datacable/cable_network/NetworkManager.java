package dev.gacbl.logicore.blocks.datacable.cable_network;

import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NetworkManager extends SavedData {
    private static final String NAME = "logicore_network_manager";
    private static final String POS_KEY = "pos";

    private final Map<UUID, ComputationNetwork> networks = new HashMap<>();

    public static NetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(NetworkManager::new, NetworkManager::load, DataFixTypes.LEVEL),
                NAME
        );
    }

    public void tick(ServerLevel level) {
        for (ComputationNetwork network : this.networks.values()) {
            network.tick(level);
        }
    }

    public void onCablePlaced(Level level, BlockPos currentPos) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(currentPos) instanceof DataCableBlockEntity currentBE)) return;

        Set<ComputationNetwork> adjacentNetworks = findAdjacentNetworks(level, currentPos);

        if (adjacentNetworks.isEmpty()) {
            // Case 0: No neighbors. Create a new network.
            ComputationNetwork newNetwork = new ComputationNetwork();
            newNetwork.addCable(currentPos);
            currentBE.setNetworkUUID(newNetwork.getNetworkID());
            this.networks.put(newNetwork.getNetworkID(), newNetwork);
            newNetwork.setDirty();
        } else if (adjacentNetworks.size() == 1) {
            // Case 1: One neighbor. Add this cable to it.
            ComputationNetwork existingNetwork = adjacentNetworks.iterator().next();
            existingNetwork.addCable(currentPos);
            currentBE.setNetworkUUID(existingNetwork.getNetworkID());
            existingNetwork.setDirty();
        } else {
            // Case 2: Multiple neighbors. Merge them.
            List<ComputationNetwork> networksToMerge = new ArrayList<>(adjacentNetworks);
            ComputationNetwork primaryNetwork = networksToMerge.getFirst();

            // Add the new cable to the primary network
            primaryNetwork.addCable(currentPos);
            currentBE.setNetworkUUID(primaryNetwork.getNetworkID());

            // Merge all other networks into the primary one
            for (int i = 1; i < networksToMerge.size(); i++) {
                ComputationNetwork networkToMerge = networksToMerge.get(i);

                // Merge network data (cables, devices, etc.)
                primaryNetwork.merge(networkToMerge);

                // Update all BEs from the old network
                for (BlockPos cablePos : networkToMerge.getCables()) {
                    if (level.getBlockEntity(cablePos) instanceof DataCableBlockEntity dbe) {
                        dbe.setNetworkUUID(primaryNetwork.getNetworkID());
                    }
                }

                // Remove the old, now-merged network
                this.networks.remove(networkToMerge.getNetworkID());
            }

            primaryNetwork.setDirty();
        }

        // Always scan neighbors of the *new* block for devices
        scanForDevices(level, currentPos, currentBE.getNetworkUUID());
        setDirty(); // Mark the manager as dirty to save changes
    }

    private Set<ComputationNetwork> findAdjacentNetworks(Level level, BlockPos pos) {
        Set<ComputationNetwork> adjacentNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);

            if (neighborBE instanceof DataCableBlockEntity neighborCable) {
                if (neighborCable.getNetworkUUID() != null) {
                    ComputationNetwork network = this.networks.get(neighborCable.getNetworkUUID());
                    if (network != null) {
                        adjacentNetworks.add(network);
                    }
                }
            }
        }
        return adjacentNetworks;
    }

    public void onCableRemoved(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof DataCableBlockEntity removedBE)) return;

        UUID networkUUID = removedBE.getNetworkUUID();
        if (networkUUID == null) return;

        ComputationNetwork network = this.networks.get(networkUUID);
        if (network == null) return;

        // Get a copy of the cables before removing the network
        Set<BlockPos> cablesToRescan = new HashSet<>(network.getCables());
        cablesToRescan.remove(pos); // Don't rescan the block that was removed

        // Remove the old network completely
        this.networks.remove(networkUUID);

        // Invalidate all BEs from that network
        for (BlockPos cablePos : cablesToRescan) {
            if (level.getBlockEntity(cablePos) instanceof DataCableBlockEntity dbe) {
                dbe.setNetworkUUID(null);
            }
        }

        // Now, treat all remaining cables as if they were just placed,
        // This will safely create new, smaller networks
        for (BlockPos cablePos : cablesToRescan) {
            if (level.getBlockEntity(cablePos) instanceof DataCableBlockEntity dbe && dbe.getNetworkUUID() == null) {
                // Only place cables that haven't already found a new network
                onCablePlaced(level, cablePos);
            }
        }

        // Also rescan neighbors for device changes
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!(neighborState.getBlock() instanceof DataCableBlock)) {
                // Neighbor is not a cable, it might be a device that was disconnected,
                // Or it might be a cable from *another* network that needs to be updated
                neighborChanged(level, neighborPos, pos);
            }
        }
        setDirty();
    }

    public void neighborChanged(Level level, BlockPos pos, BlockPos fromPos) {
        if (level.isClientSide) return;

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DataCableBlock) {
            // A cable's neighbor changed
            if (level.getBlockEntity(pos) instanceof DataCableBlockEntity dbe) {
                scanForDevices(level, pos, dbe.getNetworkUUID());
            }
        } else {
            // A device's neighbor changed
            if (level.getBlockEntity(pos) != null) {
                // This block is a device, check if a cable was removed next to it
                if (!(level.getBlockState(fromPos).getBlock() instanceof DataCableBlock)) {
                    // The block that changed *wasn't* a cable, so we don't care
                    return;
                }

                // A cable *was* at fromPos. Find any adjacent networks and notify them.
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    if (level.getBlockEntity(neighborPos) instanceof DataCableBlockEntity dbe) {
                        if (dbe.getNetworkUUID() != null) {
                            ComputationNetwork network = this.networks.get(dbe.getNetworkUUID());
                            if (network != null) {
                                network.setDirty();
                            }
                        }
                    }
                }
            }
        }
    }

    private void scanForDevices(Level level, BlockPos cablePos, UUID networkUUID) {
        if (networkUUID == null) return;
        ComputationNetwork network = this.networks.get(networkUUID);
        if (network == null) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = cablePos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!(neighborState.getBlock() instanceof DataCableBlock)) {
                // It's not a cable, check if it's a device
                network.scanDevice(level, neighborPos);
            }
        }
        network.setDirty(); // Mark network dirty to recalculate demand/capacity
    }

    public static NetworkManager load(CompoundTag tag, HolderLookup.Provider registries) {
        NetworkManager manager = new NetworkManager();
        ListTag networksTag = tag.getList("Networks", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < networksTag.size(); i++) {
            CompoundTag networkTag = networksTag.getCompound(i);
            ComputationNetwork network = new ComputationNetwork();
            network.setNetworkID(networkTag.getUUID("NetworkID"));

            // Load Cables
            ListTag cablesTag = networkTag.getList("Cables", CompoundTag.TAG_INT_ARRAY);
            cablesTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addCable(pos);
            });

            // Load Providers
            ListTag providersTag = networkTag.getList("Providers", CompoundTag.TAG_INT_ARRAY);
            providersTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addProvider(pos);
            });

            // Load Consumers
            ListTag consumersTag = networkTag.getList("Consumers", CompoundTag.TAG_INT_ARRAY);
            consumersTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addConsumer(pos);
            });

            // Load EnergySources
            ListTag energyTag = networkTag.getList("EnergySources", CompoundTag.TAG_INT_ARRAY);
            energyTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addEnergySource(pos);
            });

            network.setDirty(); // Mark as dirty to force a rebuild on the next tick
            manager.networks.put(network.getNetworkID(), network);
        }
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag networksTag = new ListTag();
        for (ComputationNetwork network : this.networks.values()) {
            CompoundTag networkTag = new CompoundTag();
            networkTag.putUUID("NetworkID", network.getNetworkID());

            // Save Cables
            ListTag cablesTag = new ListTag();
            network.getCables().forEach(pos -> cablesTag.add(NbtUtils.writeBlockPos(pos)));
            networkTag.put("Cables", cablesTag);

            // Save Providers
            ListTag providersTag = new ListTag();
            network.getProviders().forEach(pos -> providersTag.add(NbtUtils.writeBlockPos(pos)));
            networkTag.put("Providers", providersTag);

            // Save Consumers
            ListTag consumersTag = new ListTag();
            network.getConsumers().forEach(pos -> consumersTag.add(NbtUtils.writeBlockPos(pos)));
            networkTag.put("Consumers", consumersTag);

            // Save EnergySources
            ListTag energyTag = new ListTag();
            network.getEnergySources().forEach(pos -> energyTag.add(NbtUtils.writeBlockPos(pos)));
            networkTag.put("EnergySources", energyTag);

            networksTag.add(networkTag);
        }
        tag.put("Networks", networksTag);
        return tag;
    }

    public void clearAll() {
        for (ComputationNetwork network : this.networks.values()) {
            network.clearAll();
        }
        this.networks.clear();
        setDirty();
    }

    public Map<UUID, ComputationNetwork> getNetworks() {
        return networks;
    }
}
