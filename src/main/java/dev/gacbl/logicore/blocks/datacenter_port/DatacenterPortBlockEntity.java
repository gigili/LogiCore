package dev.gacbl.logicore.blocks.datacenter_port;

import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatacenterPortBlockEntity extends BlockEntity implements ICycleProvider {
    @Nullable
    private BlockPos controllerPos;

    public DatacenterPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(DatacenterPortModule.DATACENTER_PORT_BE.get(), pos, blockState);
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(@Nullable BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        setChanged();

        Level level = getLevel();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public long getCyclesAvailable() {
        if (level == null) return 0;
        if (controllerPos == null) return 0;
        if (!isValidState()) return 0;

        long total = 0;
        if (level.getBlockEntity(controllerPos) instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
            for (BlockPos pos : controllerBlockEntity.getInteriorProviders()) {
                if (level.getBlockEntity(pos) instanceof ICycleProvider provider) {
                    total += provider.getCyclesAvailable();
                }
            }
        }
        return total;
    }

    @Override
    public long getCycleCapacity() {
        if (level == null) return 0;
        if (controllerPos == null) return 0;
        if (!isValidState()) return 0;

        long total = 0;
        if (level.getBlockEntity(controllerPos) instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
            for (BlockPos pos : controllerBlockEntity.getInteriorProviders()) {
                if (level.getBlockEntity(pos) instanceof ICycleProvider provider) {
                    total += provider.getCycleCapacity();
                }
            }
        }
        return total;
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        if (level == null) return 0;
        if (controllerPos == null) return 0;
        if (!isValidState()) return 0;

        long extractedTotal = 0;
        long remainingNeeded = maxExtract;

        if (level.getBlockEntity(controllerPos) instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
            List<BlockPos> providers = new ArrayList<>(controllerBlockEntity.getInteriorProviders());

            Collections.shuffle(providers);

            for (BlockPos pos : providers) {
                if (remainingNeeded <= 0) break;

                if (level.getBlockEntity(pos) instanceof ICycleProvider provider) {
                    long pulled = provider.extractCycles(remainingNeeded, simulate);
                    extractedTotal += pulled;
                    remainingNeeded -= pulled;
                }
            }
        }
        return extractedTotal;
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        return 0;
    }

    private Boolean isValidState() {
        if (level == null) return false;
        if (this.controllerPos == null) return false;

        if (level.getBlockEntity(this.controllerPos) instanceof DatacenterControllerBlockEntity dcbe) {
            return dcbe.isFormed;
        }

        return false;
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
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ControllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        } else {
            controllerPos = null;
        }
    }
}
