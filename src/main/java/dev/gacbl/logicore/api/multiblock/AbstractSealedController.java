package dev.gacbl.logicore.api.multiblock;

import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncMultiblockDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSealedController extends BlockEntity {

    public boolean isFormed = false;
    protected BlockPos minPos = BlockPos.ZERO;
    protected BlockPos maxPos = BlockPos.ZERO;

    // Mathematical Index (Efficient memory usage)
    private int validationIndex = 0;

    @Nullable
    public MultiblockValidationException lastException = null;

    public AbstractSealedController(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract boolean isFrameBlock(BlockState state);

    protected abstract boolean isWallBlock(BlockState state);

    protected abstract boolean isInteriorBlock(BlockState state);

    protected abstract void onStructureFormed();

    protected abstract void onStructureBroken();

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (isFormed) {
            validateBatch(level, pos);
        }
    }

    private void validateBatch(Level level, BlockPos controllerPos) {
        int width = maxPos.getX() - minPos.getX() + 1;
        int height = maxPos.getY() - minPos.getY() + 1;
        int depth = maxPos.getZ() - minPos.getZ() + 1;

        if (width <= 0 || height <= 0 || depth <= 0) {
            breakStructure(new MultiblockValidationException("errors.logicore.multiblock.invalid_dimensions"));
            return;
        }

        int totalBlocks = width * height * depth;
        int checksPerTick = Math.max(4, totalBlocks / 100);

        for (int i = 0; i < checksPerTick; i++) {
            validationIndex++;
            if (validationIndex >= totalBlocks) {
                validationIndex = 0;
            }

            int x = (validationIndex % width) + minPos.getX();
            int y = ((validationIndex / width) % height) + minPos.getY();
            int z = (validationIndex / (width * height)) + minPos.getZ();

            BlockPos p = new BlockPos(x, y, z);

            if (!level.isLoaded(p)) continue;
            if (p.equals(this.worldPosition)) continue;

            BlockState checkState = level.getBlockState(p);

            boolean onMinX = x == minPos.getX();
            boolean onMaxX = x == maxPos.getX();
            boolean onMinY = y == minPos.getY();
            boolean onMaxY = y == maxPos.getY();
            boolean onMinZ = z == minPos.getZ();
            boolean onMaxZ = z == maxPos.getZ();

            int boundaryCount = (onMinX ? 1 : 0) + (onMaxX ? 1 : 0) +
                    (onMinY ? 1 : 0) + (onMaxY ? 1 : 0) +
                    (onMinZ ? 1 : 0) + (onMaxZ ? 1 : 0);

            boolean isAnyBoundary = boundaryCount >= 1;
            boolean isEdge = boundaryCount >= 2;

            if (isAnyBoundary) {
                if (checkState.getBlock() == this.getBlockState().getBlock()) {
                    breakStructure(new MultiblockValidationException("errors.logicore.multiblock.duplicate_controller", p, checkState));
                    return;
                }

                if (isEdge) {
                    if (!isFrameBlock(checkState)) {
                        breakStructure(new MultiblockValidationException("errors.logicore.multiblock.broken_frame", p, checkState));
                        return;
                    }
                } else {
                    if (!isWallBlock(checkState)) {
                        breakStructure(new MultiblockValidationException("errors.logicore.multiblock.broken_wall", p, checkState));
                        return;
                    }
                }
            } else {
                if (!isInteriorBlock(checkState)) {
                    breakStructure(new MultiblockValidationException("errors.logicore.multiblock.invalid_interior", p, checkState));
                    return;
                } else {
                    if (level.getBlockEntity(p) instanceof CoreCycleProviderBlockEntity cBe) {
                        if (cBe.dataCenterController != controllerPos) {
                            cBe.setDataCenterController(controllerPos);
                        }
                    }
                }
            }
        }
    }

    public void formStructure(BlockPos min, BlockPos max) {
        this.isFormed = true;
        this.minPos = min;
        this.maxPos = max;
        this.validationIndex = 0;
        this.lastException = null;
        onStructureFormed();
        setChanged();
        syncData();
    }

    public void breakStructure(@Nullable MultiblockValidationException e) {
        this.isFormed = false;
        this.lastException = e;
        onStructureBroken();
        setChanged();
        syncData();
    }

    public void syncData() {
        if (level != null && !level.isClientSide) {
            String errorMsg = "";
            BlockPos errorPos = BlockPos.ZERO;

            if (lastException != null) {
                errorMsg = lastException.message;
                if (lastException.pos != null) {
                    errorPos = lastException.pos;
                }
            }

            PacketHandler.sendToClientsTrackingChunk(level, this.worldPosition,
                    new SyncMultiblockDataPayload(this.worldPosition, this.isFormed, this.minPos, this.maxPos, errorMsg, errorPos)
            );
        }
    }

    public void setClientData(boolean isFormed, BlockPos min, BlockPos max, String errorMsg, BlockPos errorPos) {
        this.isFormed = isFormed;
        this.minPos = min;
        this.maxPos = max;

        if (errorMsg != null && !errorMsg.isEmpty()) {
            this.lastException = new MultiblockValidationException(errorMsg, errorPos, null);
        } else {
            this.lastException = null;
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Formed", isFormed);
        if (isFormed) {
            tag.putLong("MinPos", minPos.asLong());
            tag.putLong("MaxPos", maxPos.asLong());
        }

        if (lastException != null) {
            tag.putString("lastExceptionMessage", lastException.message);
            if (lastException.pos != null) {
                tag.put("lastExceptionBlockPos", NbtUtils.writeBlockPos(lastException.pos));
            }
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        isFormed = tag.getBoolean("Formed");
        if (isFormed) {
            minPos = BlockPos.of(tag.getLong("MinPos"));
            maxPos = BlockPos.of(tag.getLong("MaxPos"));
        }

        if (tag.contains("lastExceptionMessage")) {
            String msg = tag.getString("lastExceptionMessage");
            BlockPos p = tag.contains("lastExceptionBlockPos") ? NbtUtils.readBlockPos(tag, "lastExceptionBlockPos").orElse(null) : null;
            lastException = new MultiblockValidationException(msg, p, null);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
