package dev.gacbl.logicore.blocks.datacable.cable_network;

import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class NetworkManager extends SavedData {
    private static final String POS_KEY = "pos";

    private final Map<UUID, ComputationNetwork> networks = new HashMap<>();

    public static final SavedDataType<NetworkManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("logicore", "network_manager"),
            NetworkManager::new,
            CompoundTag.CODEC.xmap(NetworkManager::fromTag, NetworkManager::toTag)
    );

    public static NetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void tick(ServerLevel level) {
        for (ComputationNetwork network : this.networks.values()) {
            network.tick(level);
        }
    }

    public void onCablePlaced(Level level, BlockPos currentPos) {
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(currentPos) instanceof DataCableBlockEntity currentBE)) return;

        Set<ComputationNetwork> adjacentNetworks = findAdjacentNetworks(level, currentPos);

        if (adjacentNetworks.isEmpty()) {
            ComputationNetwork newNetwork = new ComputationNetwork();
            newNetwork.addCable(currentPos);
            currentBE.setNetworkUUID(newNetwork.getNetworkID());
            this.networks.put(newNetwork.getNetworkID(), newNetwork);
            newNetwork.setDirty();
        } else if (adjacentNetworks.size() == 1) {
            ComputationNetwork existingNetwork = adjacentNetworks.iterator().next();
            existingNetwork.addCable(currentPos);
            currentBE.setNetworkUUID(existingNetwork.getNetworkID());
            existingNetwork.setDirty();
        } else {
            List<ComputationNetwork> networksToMerge = new ArrayList<>(adjacentNetworks);
            ComputationNetwork primaryNetwork = networksToMerge.getFirst();

            primaryNetwork.addCable(currentPos);
            currentBE.setNetworkUUID(primaryNetwork.getNetworkID());

            for (int i = 1; i < networksToMerge.size(); i++) {
                ComputationNetwork networkToMerge = networksToMerge.get(i);
                primaryNetwork.merge(networkToMerge);

                for (BlockPos cablePos : networkToMerge.getCables()) {
                    if (level.getBlockEntity(cablePos) instanceof DataCableBlockEntity dbe) {
                        dbe.setNetworkUUID(primaryNetwork.getNetworkID());
                    }
                }

                this.networks.remove(networkToMerge.getNetworkID());
            }

            primaryNetwork.setDirty();
        }

        scanForDevices(level, currentPos, currentBE.getNetworkUUID());
        setDirty();
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
        if (level.isClientSide()) return;
        UUID networkUUID = null;
        if (level.getBlockEntity(pos) instanceof DataCableBlockEntity removedBE) {
            networkUUID = removedBE.getNetworkUUID();
        }

        if (networkUUID == null) {
            for (Map.Entry<UUID, ComputationNetwork> entry : this.networks.entrySet()) {
                if (entry.getValue().getCables().contains(pos)) {
                    networkUUID = entry.getKey();
                    break;
                }
            }
        }

        if (networkUUID == null) return;

        ComputationNetwork network = this.networks.get(networkUUID);
        if (network == null) return;

        Set<BlockPos> cablesToRescan = new HashSet<>(network.getCables());
        cablesToRescan.remove(pos);

        this.networks.remove(networkUUID);

        for (BlockPos cablePos : cablesToRescan) {
            if (level.getBlockEntity(cablePos) instanceof DataCableBlockEntity dbe) {
                dbe.setNetworkUUID(null);
            }
        }

        for (BlockPos cablePos : cablesToRescan) {
            if (level.getBlockEntity(cablePos) instanceof DataCableBlockEntity dbe && dbe.getNetworkUUID() == null) {
                onCablePlaced(level, cablePos);
            }
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!(neighborState.getBlock() instanceof DataCableBlock)) {
                neighborChanged(level, neighborPos, pos);
            }
        }
        setDirty();
    }

    public void neighborChanged(Level level, BlockPos pos, BlockPos fromPos) {
        if (level.isClientSide()) return;

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DataCableBlock) {
            if (level.getBlockEntity(pos) instanceof DataCableBlockEntity dbe) {
                scanForDevices(level, pos, dbe.getNetworkUUID());
            }
        } else {
            if (level.getBlockEntity(pos) != null) {
                if (!(level.getBlockState(fromPos).getBlock() instanceof DataCableBlock)) {
                    return;
                }

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

    public void scanForDevices(Level level, BlockPos cablePos, UUID networkUUID) {
        if (networkUUID == null) return;
        ComputationNetwork network = this.networks.get(networkUUID);
        if (network == null) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = cablePos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!(neighborState.getBlock() instanceof DataCableBlock)) {
                network.scanDevice(level, neighborPos);
            }
        }
        network.setDirty();
    }

    public static NetworkManager fromTag(CompoundTag tag) {
        NetworkManager manager = new NetworkManager();
        ListTag networksTag = tag.getListOrEmpty("Networks");

        for (int i = 0; i < networksTag.size(); i++) {
            CompoundTag networkTag = networksTag.getCompoundOrEmpty(i);
            ComputationNetwork network = new ComputationNetwork();
            network.setNetworkID(new UUID(networkTag.getLongOr("NetworkIDMost", 0L), networkTag.getLongOr("NetworkIDLeast", 0L)));

            ListTag cablesTag = networkTag.getListOrEmpty("Cables");
            cablesTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addCable(pos);
            });

            ListTag providersTag = networkTag.getListOrEmpty("Providers");
            providersTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addProvider(pos);
            });

            ListTag consumersTag = networkTag.getListOrEmpty("Consumers");
            consumersTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addConsumer(pos);
            });

            ListTag energyTag = networkTag.getListOrEmpty("EnergySources");
            energyTag.forEach(nbt -> {
                int[] arrayTag = ((IntArrayTag) nbt).getAsIntArray();
                BlockPos pos = new BlockPos(arrayTag[0], arrayTag[1], arrayTag[2]);
                network.addEnergySource(pos);
            });

            network.setDirty();
            manager.networks.put(network.getNetworkID(), network);
        }
        return manager;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag networksTag = new ListTag();
        for (ComputationNetwork network : this.networks.values()) {
            CompoundTag networkTag = new CompoundTag();
            networkTag.putLong("NetworkIDMost", network.getNetworkID().getMostSignificantBits());
            networkTag.putLong("NetworkIDLeast", network.getNetworkID().getLeastSignificantBits());

            ListTag cablesTag = new ListTag();
            network.getCables().forEach(pos -> cablesTag.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()})));
            networkTag.put("Cables", cablesTag);

            ListTag providersTag = new ListTag();
            network.getProviders().forEach(pos -> providersTag.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()})));
            networkTag.put("Providers", providersTag);

            ListTag consumersTag = new ListTag();
            network.getConsumers().forEach(pos -> consumersTag.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()})));
            networkTag.put("Consumers", consumersTag);

            ListTag energyTag = new ListTag();
            network.getEnergySources().forEach(pos -> energyTag.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()})));
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
