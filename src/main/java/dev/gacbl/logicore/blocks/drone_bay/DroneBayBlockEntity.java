package dev.gacbl.logicore.blocks.drone_bay;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroneBayBlockEntity extends BlockEntity implements ICycleConsumer {
    private final CycleStorage cycleStorage = new CycleStorage(500_000L, 10_000L, 10_000L, 0, 500);
    private String dockedName = "";
    private long lastSyncedCycles = -1;

    public DroneBayBlockEntity(BlockPos pos, BlockState blockState) {
        super(DroneBayModule.DRONE_BAY_BE.get(), pos, blockState);
    }

    @Override
    public long getCycleDemand() {
        return cycleStorage.getCycleDemand();
    }

    @Override
    public long getCyclesStored() {
        return cycleStorage.getCyclesStored();
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        return cycleStorage.receiveCycles(receive, simulate);
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        return cycleStorage.extractCycles(maxExtract, simulate);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        long currentCycles = cycleStorage.getCyclesStored();
        if (currentCycles != lastSyncedCycles) {
            lastSyncedCycles = currentCycles;
            syncData();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("cycles", this.cycleStorage.serializeNBT(registries));
        tag.putString("DockedName", dockedName);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("cycles", 10)) {
            this.cycleStorage.deserializeNBT(registries, (CompoundTag) tag.get("cycles"));
        }

        if (tag.contains("DockedName")) {
            this.dockedName = tag.getString("DockedName");
        }
    }

    public @Nullable ICycleStorage getCycleStorage() {
        return cycleStorage;
    }

    public String getDockedName() {
        return dockedName;
    }

    public void setDockedName(String name) {
        if (!this.dockedName.equals(name)) {
            this.dockedName = name;
            syncData();
        }
    }

    public void clearDockedName() {
        if (!this.dockedName.isEmpty()) {
            this.dockedName = "";
            syncData();
        }
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
