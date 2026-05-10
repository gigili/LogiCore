package dev.gacbl.logicore.blocks.datacenter_port;

import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    public long getCpuCount() {
        if (level == null) return 0;
        if (!validController() || controllerPos == null) return 0;
        if (!isValidState()) return 0;

        long total = 0;
        if (level.getBlockEntity(controllerPos) instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
            for (BlockPos pos : controllerBlockEntity.getInteriorProviders()) {
                if (level.getBlockEntity(pos) instanceof CoreCycleProviderBlockEntity cBe) {
                    total += cBe.getProcessorCount();
                }
            }
        }
        return total;
    }

    public long getCpuMaxCount() {
        if (level == null) return 0;
        if (!validController() || controllerPos == null) return 0;
        if (!isValidState()) return 0;

        long total = 0;
        if (level.getBlockEntity(controllerPos) instanceof DatacenterControllerBlockEntity controllerBlockEntity) {
            for (BlockPos pos : controllerBlockEntity.getInteriorProviders()) {
                if (level.getBlockEntity(pos) instanceof CoreCycleProviderBlockEntity cBe) {
                    total += cBe.getMaxProcessorCount();
                }
            }
        }
        return total;
    }

    @Override
    public long getCyclesAvailable() {
        if (level == null) return 0;
        if (!validController() || controllerPos == null) return 0;
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
        if (!validController() || controllerPos == null) return 0;
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
        if (!validController() || controllerPos == null) return 0;
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
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (controllerPos != null) {
            output.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    private boolean validController() {
        return controllerPos != null && level != null && level.isLoaded(controllerPos) && level.getBlockEntity(controllerPos) instanceof DatacenterControllerBlockEntity;
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        if (input.getLong("ControllerPos").isPresent()) {
            controllerPos = BlockPos.of(input.getLongOr("ControllerPos", 0L));
        } else {
            controllerPos = null;
        }
    }
}
