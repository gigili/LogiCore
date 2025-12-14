package dev.gacbl.logicore.blocks.cloud_interface;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.ComputationNetwork;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.core.ModCapabilities;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncPlayerCyclesPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CloudInterfaceBlockEntity extends BlockEntity implements ICycleProvider {
    private UUID ownerUUID;

    public final CycleStorage cycleStorage = new CycleStorage(1_000_000, 100_000, 100_000);

    public CloudInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(CloudInterfaceModule.CLOUD_INTERFACE_BE.get(), pos, blockState);
    }

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CloudInterfaceBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel) || be.ownerUUID == null) return;

        // Use UUID as key (simplification for offline players).
        // If you want strict team support for offline players, you'd need to look up their team via scoreboard here.
        String storageKey = be.ownerUUID.toString();

        // Retrieve the global data
        CycleSavedData savedData = CycleSavedData.get(serverLevel);

        // 1. UPLOAD LOGIC (Back of the block)
        Direction facing = state.getValue(CloudInterfaceBlock.FACING);
        Direction inputSide = facing.getOpposite(); // The side "behind" the interface
        BlockPos inputPos = pos.relative(inputSide);

        // Check for provider at the back
        ICycleProvider provider = level.getCapability(ModCapabilities.CYCLE_PROVIDER, inputPos, inputSide.getOpposite());
        if (provider != null) {
            long maxUpload = 10000; // Rate limit
            long extracted = provider.extractCycles(maxUpload, false);
            if (extracted > 0) {
                savedData.modifyCyclesByKey(storageKey, extracted);
                syncToOwner(serverLevel, be.ownerUUID, savedData.getCyclesByKey(storageKey));
            }
        }

        if (level.getBlockEntity(inputPos) instanceof DataCableBlockEntity dc) {
            if (dc.getNetworkUUID() != null) {
                UUID networkUUID = dc.getNetworkUUID();
                NetworkManager manager = NetworkManager.get(serverLevel);
                if (manager.getNetworks().containsKey(networkUUID)) {
                    ComputationNetwork network = manager.getNetworks().get(networkUUID);
                    long extracted = network.extractCycles(serverLevel, 10000);
                    if (extracted > 0) {
                        savedData.modifyCyclesByKey(storageKey, extracted);
                        syncToOwner(serverLevel, be.ownerUUID, savedData.getCyclesByKey(storageKey));
                    }
                }
            }
        }
    }

    private static void syncToOwner(ServerLevel level, UUID ownerUUID, long newValue) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (player != null) {
            PacketHandler.sendToPlayer(player, new SyncPlayerCyclesPayload(newValue));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    public long getCyclesAvailable() {
        if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;
        CycleSavedData savedData = CycleSavedData.get(serverLevel);
        return savedData.getCyclesByKey(ownerUUID.toString());
    }

    @Override
    public long getCycleCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;
        CycleSavedData savedData = CycleSavedData.get(serverLevel);
        Player player = serverLevel.getPlayerByUUID(ownerUUID);
        long extracted = savedData.extractCycles(player, maxExtract, simulate);
        if (extracted > 0) {
            syncToOwner(serverLevel, ownerUUID, savedData.getCyclesByKey(CycleSavedData.getStorageKey(player)));
        }
        return extracted;
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;

        if (!simulate) {
            String storageKey = ownerUUID.toString();
            CycleSavedData savedData = CycleSavedData.get(serverLevel);
            savedData.modifyCyclesByKey(storageKey, receive);
            Player player = serverLevel.getPlayerByUUID(ownerUUID);
            syncToOwner(serverLevel, ownerUUID, savedData.getCyclesByKey(CycleSavedData.getStorageKey(player)));
        }

        return receive;
    }
}
