package dev.gacbl.logicore.blocks.drone_bay;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroneBayBlockEntity extends BlockEntity implements ICycleConsumer {
    private final CycleStorage cycleStorage;

    public DroneBayBlockEntity(BlockPos pos, BlockState blockState) {
        super(DroneBayModule.DRONE_BAY_BE.get(), pos, blockState);
        cycleStorage = new CycleStorage(500_000L, 10_000L, 10_000L, 0, 500);
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

    public void tick(Level level, BlockPos pos, BlockState state) {

    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("cycles", this.cycleStorage.serializeNBT(registries));
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("cycles", 10)) {
            this.cycleStorage.deserializeNBT(registries, (CompoundTag) tag.get("cycles"));
        }
    }

    public @Nullable ICycleStorage getCycleStorage() {
        return cycleStorage;
    }
}
