package dev.gacbl.logicore.blocks.cloud_interface;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.compat.ae2.Ae2Helper;
import dev.gacbl.logicore.api.compat.ae2.IGridNodeService;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.ComputationNetwork;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CloudInterfaceBlockEntity extends BlockEntity implements ICycleProvider {
    private UUID ownerUUID;
    private IGridNodeService ae2Service;
    private ItemStack buffer = ItemStack.EMPTY;

    public boolean insert(ItemStack stack) {
        if (!buffer.isEmpty()) return false;
        buffer = stack.copy();
        setChanged();
        return true;
    }

    public ItemStack extract() {
        ItemStack out = buffer;
        buffer = ItemStack.EMPTY;
        setChanged();
        return out;
    }

    public boolean hasItem() {
        return !buffer.isEmpty();
    }

    public CloudInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(CloudInterfaceModule.CLOUD_INTERFACE_BE.get(), pos, blockState);
        if (ModList.get().isLoaded("ae2")) {
            this.ae2Service = Ae2Helper.createService(this);
        }
    }

    public IGridNodeService getAe2Service() {
        return ae2Service;
    }

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
        setChanged();
    }

    public UUID getOwner() {
        return this.ownerUUID;
    }

    private String getStorageKey(ServerLevel serverLevel) {
        if (ownerUUID == null) return "invalid";
        return CycleSavedData.getKey(serverLevel, ownerUUID);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CloudInterfaceBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel) || be.ownerUUID == null) return;

        if (be.ae2Service != null) {
            be.ae2Service.serverTick();
        }

        long maxUpload = Config.CI_MAX_TRANSFER_RATE.get();
        String storageKey = be.getStorageKey(serverLevel);
        CycleSavedData savedData = CycleSavedData.get(serverLevel);

        Direction facing = state.getValue(CloudInterfaceBlock.FACING);
        Direction inputSide = facing.getOpposite();
        BlockPos inputPos = pos.relative(inputSide);

        ICycleProvider provider = level.getCapability(ModCapabilities.CYCLE_PROVIDER, inputPos, inputSide.getOpposite());
        if (provider != null) {
            long extracted = provider.extractCycles(maxUpload, false);

            if (extracted > 0) {
                savedData.modifyCycles(serverLevel, storageKey, extracted);
            }
        }

        if (level.getBlockEntity(inputPos) instanceof DataCableBlockEntity dc) {
            if (dc.getNetworkUUID() != null) {
                UUID networkUUID = dc.getNetworkUUID();
                NetworkManager manager = NetworkManager.get(serverLevel);

                if (manager.getNetworks().containsKey(networkUUID)) {
                    ComputationNetwork network = manager.getNetworks().get(networkUUID);
                    long extracted = network.extractCycles(serverLevel, maxUpload);

                    if (extracted > 0) {
                        savedData.modifyCycles(serverLevel, storageKey, extracted);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (ae2Service != null) {
            ae2Service.save(tag, registries);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        if (ae2Service != null) {
            ae2Service.load(tag, registries);
        }
    }

    @Override
    public long getCyclesAvailable() {
        if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;
        return CycleSavedData.get(serverLevel).getCyclesByKeyString(ownerUUID.toString());
    }

    @Override
    public long getCycleCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;

        CycleSavedData savedData = CycleSavedData.get(serverLevel);
        String key = getStorageKey(serverLevel);
        long current = savedData.getCyclesByKeyString(key);
        long extracted = Math.min(current, maxExtract);

        if (!simulate && extracted > 0) {
            savedData.modifyCycles(serverLevel, key, -extracted);
        }
        return extracted;
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;

        if (!simulate) {
            String key = getStorageKey(serverLevel);
            CycleSavedData.get(serverLevel).modifyCycles(serverLevel, key, receive);
        }
        return receive;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (ae2Service != null) {
            ae2Service.onRemove();
        }
    }
}
