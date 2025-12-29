package dev.gacbl.logicore.blocks.cloud_interface;

import dev.gacbl.logicore.Config;
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

public class CloudInterfaceBlockEntity extends BlockEntity {
    private UUID ownerUUID;
    private IGridNodeService ae2Service;
    private ItemStack buffer = ItemStack.EMPTY;

    private long bufferedCycles = 0;
    private boolean isBackConnected = false;

    private final ICycleProvider UPLOAD_HANDLER = new UploadHandler();
    private final ICycleProvider DOWNLOAD_HANDLER = new DownloadHandler();

    public CloudInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(CloudInterfaceModule.CLOUD_INTERFACE_BE.get(), pos, state);
        if (ModList.get().isLoaded("ae2")) {
            this.ae2Service = dev.gacbl.logicore.api.compat.ae2.Ae2Helper.createService(this);
        }
    }

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

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
        setChanged();
    }

    public UUID getOwner() {
        return ownerUUID;
    }

    public IGridNodeService getAe2Service() {
        return ae2Service;
    }

    private String getStorageKey(ServerLevel serverLevel) {
        if (ownerUUID == null) return "invalid";
        return CycleSavedData.getKey(serverLevel, ownerUUID);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CloudInterfaceBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) return;

        long maxUpload = Config.CI_MAX_TRANSFER_RATE.get();

        Direction facing = state.getValue(CloudInterfaceBlock.FACING);
        Direction inputSide = facing.getOpposite();
        BlockPos inputPos = pos.relative(inputSide);

        ICycleProvider provider = level.getCapability(ModCapabilities.CYCLE_PROVIDER, inputPos, inputSide.getOpposite());
        if (provider != null) {
            long extracted = provider.extractCycles(maxUpload, false);

            if (extracted > 0) {
                be.bufferedCycles += extracted;
            }
        }

        if (level.getBlockEntity(inputPos) instanceof DataCableBlockEntity dc) {
            if (dc.getNetworkUUID() != null) {
                UUID networkUUID = dc.getNetworkUUID();
                NetworkManager manager = NetworkManager.get(sl);

                if (manager.getNetworks().containsKey(networkUUID)) {
                    ComputationNetwork network = manager.getNetworks().get(networkUUID);
                    long extracted = network.extractCycles(sl, maxUpload);

                    if (extracted > 0) {
                        be.bufferedCycles += extracted;
                    }
                }
            }
        }


        if (level.getGameTime() % 20 == 0) { // Check every second, not every tick
            if (be.bufferedCycles != 0) {
                if (be.ownerUUID != null) {
                    String key = CycleSavedData.getKey(sl, be.ownerUUID);
                    CycleSavedData.get(sl).modifyCycles(sl, key, be.bufferedCycles);
                    be.bufferedCycles = 0;
                }
            }
        }

        if (be.ae2Service != null) {
            be.ae2Service.serverTick();
        }
    }

    public ICycleProvider getCycleCapability(Direction ctx) {
        if (level == null || level.isClientSide) return null;

        Direction facing = getBlockState().getValue(CloudInterfaceBlock.FACING);
        Direction opposite = facing.getOpposite();
        BlockPos pos = getBlockPos().relative(opposite);
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof DataCableBlockEntity dc) {
            return UPLOAD_HANDLER;
        }

        for (Direction dir : Direction.values()) {
            BlockPos pX = getBlockPos().relative(dir);
            BlockEntity beX = level.getBlockEntity(pX);
            if (dir != opposite) {
                if (beX == null) continue;
                if (beX instanceof DataCableBlockEntity) return DOWNLOAD_HANDLER;
            }
        }

        return null;
    }

    private class UploadHandler implements ICycleProvider {
        @Override
        public long receiveCycles(long receive, boolean simulate) {
            if (ownerUUID == null) return 0;
            if (!simulate) {
                // Buffer the addition
                bufferedCycles += receive;
                setChanged();
            }
            return receive;
        }

        @Override
        public long extractCycles(long maxExtract, boolean simulate) {
            return 0; // Cannot extract from the upload side
        }

        @Override
        public long getCyclesAvailable() {
            return 0; // Pretend empty to prevent extraction attempts
        }

        @Override
        public long getCycleCapacity() {
            return Long.MAX_VALUE;
        }
    }

    private class DownloadHandler implements ICycleProvider {
        @Override
        public long receiveCycles(long receive, boolean simulate) {
            return 0; // Cannot insert into the download side
        }

        @Override
        public long extractCycles(long maxExtract, boolean simulate) {
            if (ownerUUID == null || level == null) return 0;
            if (!(level instanceof ServerLevel sl)) return 0;

            long extractedTotal = 0;
            long remainingNeeded = maxExtract;

            if (bufferedCycles > 0) {
                long fromBuffer = Math.min(bufferedCycles, remainingNeeded);
                if (!simulate) {
                    bufferedCycles -= fromBuffer;
                    setChanged();
                }
                remainingNeeded -= fromBuffer;
                extractedTotal += fromBuffer;
            }

            if (remainingNeeded > 0) {
                String key = CycleSavedData.getKey(sl, ownerUUID);

                long globalBalance = CycleSavedData.get(sl).getCyclesByKeyString(key);
                long fromGlobal = Math.min(globalBalance, remainingNeeded);

                if (!simulate && fromGlobal > 0) {
                    //CycleSavedData.get(sl).modifyCycles(sl, key, -fromGlobal);
                }
                extractedTotal += fromGlobal;
            }

            return extractedTotal;
        }

        @Override
        public long getCyclesAvailable() {
            if (ownerUUID == null || level == null) return 0;
            if (!(level instanceof ServerLevel sl)) return 0;

            String key = CycleSavedData.getKey(sl, ownerUUID);
            long global = CycleSavedData.get(sl).getCyclesByKeyString(key);

            return global + bufferedCycles;
        }

        @Override
        public long getCycleCapacity() {
            return Long.MAX_VALUE;
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        if (ae2Service != null) {
            ae2Service.load(tag, registries);
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
    public void setRemoved() {
        super.setRemoved();
        if (ae2Service != null) {
            ae2Service.onRemove();
        }
    }
}
