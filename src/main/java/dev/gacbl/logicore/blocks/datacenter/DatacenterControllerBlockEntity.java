package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.api.multiblock.MultiblockValidationException;
import dev.gacbl.logicore.api.multiblock.MultiblockValidator;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.core.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class DatacenterControllerBlockEntity extends AbstractSealedController implements ICycleProvider {
    private final Set<BlockPos> interiorProviders = new HashSet<>();
    private boolean cacheDirty = true;

    public DatacenterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DatacenterModule.DATACENTER_CONTROLLER_BE.get(), pos, state);
    }

    @Override
    protected boolean isFrameBlock(BlockState state) {
        return state.is(ModTags.Blocks.VALID_DATACENTER_FRAME_BLOCK);
    }

    @Override
    protected boolean isWallBlock(BlockState state) {
        return state.is(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK);
    }

    @Override
    protected boolean isInteriorBlock(BlockState state) {
        return state.isAir() || state.is(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK);
    }

    private boolean isControllerBlock(BlockState state) {
        return state.getBlock() == DatacenterModule.DATACENTER_CONTROLLER.get();
    }

    public void attemptFormation() {
        if (level == null || level.isClientSide) return;

        Direction facing = this.getBlockState().getValue(BlockStateProperties.FACING);

        try {
            MultiblockValidator.ValidationResult result = MultiblockValidator.detectRoom(
                    level,
                    worldPosition,
                    facing,
                    this::isFrameBlock,
                    this::isWallBlock,
                    this::isInteriorBlock,
                    this::isControllerBlock,
                    Config.DATACENTER_MIN_MULTIBLOCK_SIZE.get(),
                    Config.DATACENTER_MAX_MULTIBLOCK_SIZE.get()
            );
            formStructure(result.min, result.max);

        } catch (MultiblockValidationException e) {
            breakStructure(e);
        }
    }

    @Override
    protected void onStructureFormed() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(DatacenterControllerBlock.FORMED, true), 3);
        }
    }

    @Override
    protected void onStructureBroken() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(DatacenterControllerBlock.FORMED, false), 3);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    /* Cycle setup */
    @Override
    public long getCyclesAvailable() {
        if (level == null) return 0;

        validateCacheIfNeeded();
        long total = 0;
        for (BlockPos pos : interiorProviders) {
            if (level.getBlockEntity(pos) instanceof ICycleProvider provider) {
                total += provider.getCyclesAvailable();
            }
        }
        return total;
    }

    @Override
    public long getCycleCapacity() {
        if (level == null) return 0;

        validateCacheIfNeeded();
        long total = 0;
        for (BlockPos pos : interiorProviders) {
            if (level.getBlockEntity(pos) instanceof ICycleProvider provider) {
                total += provider.getCycleCapacity();
            }
        }
        return total;
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        if (!isFormed || level == null) return 0;

        validateCacheIfNeeded();

        long extractedTotal = 0;
        long remainingNeeded = maxExtract;

        for (BlockPos pos : interiorProviders) {
            if (remainingNeeded <= 0) break;

            if (level.getBlockEntity(pos) instanceof ICycleProvider provider) {
                long pulled = provider.extractCycles(remainingNeeded, simulate);
                extractedTotal += pulled;
                remainingNeeded -= pulled;
            }
        }

        return extractedTotal;
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        return 0;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (level.getGameTime() % 100 == 0) {
            this.cacheDirty = true;
        }
    }

    private void validateCacheIfNeeded() {
        if (!cacheDirty || level == null || !isFormed) return;

        interiorProviders.clear();

        BlockPos.betweenClosedStream(minPos, maxPos).forEach(pos -> {
            if (level.getBlockEntity(pos) instanceof CoreCycleProviderBlockEntity) {
                interiorProviders.add(pos.immutable());
            }
        });

        cacheDirty = false;
    }
}
