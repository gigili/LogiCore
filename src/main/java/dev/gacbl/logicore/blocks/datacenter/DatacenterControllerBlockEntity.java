package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.api.multiblock.MultiblockValidationException;
import dev.gacbl.logicore.api.multiblock.MultiblockValidator;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.core.ModTags;
import dev.gacbl.logicore.network.payload.SyncMultiblockPortsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class DatacenterControllerBlockEntity extends AbstractSealedController {
    private final Set<BlockPos> interiorProviders = new HashSet<>();
    private final Set<BlockPos> ports = new HashSet<>();
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
            this.cacheDirty = true;
            this.validateCacheIfNeeded();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.connection.send(new SyncMultiblockPortsPayload(this.worldPosition, this.isFormed, this.ports));
            }
        }
    }

    @Override
    protected void onStructureBroken() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(DatacenterControllerBlock.FORMED, false), 3);
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.connection.send(new SyncMultiblockPortsPayload(this.worldPosition, this.isFormed, this.ports));
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("Formed", isFormed);

        ListTag portsTag = new ListTag();
        for (BlockPos portPos : ports) {
            portsTag.add(LongTag.valueOf(portPos.asLong()));
        }
        tag.put("Ports", portsTag);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        isFormed = tag.getBoolean("Formed");
        ports.clear();

        ListTag portsTag = tag.getList("Ports", Tag.TAG_LONG);
        for (Tag value : portsTag) {
            ports.add(BlockPos.of(((LongTag) value).getAsLong()));
        }
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (level.getGameTime() % 100 == 0) {
            this.cacheDirty = true;
            validateCacheIfNeeded();
        }
    }

    private void validateCacheIfNeeded() {
        if (!cacheDirty || level == null || !isFormed) return;

        interiorProviders.clear();
        ports.clear();

        BlockPos.betweenClosedStream(minPos, maxPos).forEach(pos -> {
            if (level.getBlockEntity(pos) instanceof CoreCycleProviderBlockEntity) {
                interiorProviders.add(pos.immutable());
            }

            if (level.getBlockEntity(pos) instanceof DatacenterPortBlockEntity) {
                ports.add(pos.immutable());
            }
        });

        cacheDirty = false;
    }

    public Set<BlockPos> getInteriorProviders() {
        return this.interiorProviders;
    }

    public Set<BlockPos> getPorts() {
        return this.ports;
    }

    public void applyMultiblockState(boolean formed, Set<BlockPos> newPorts) {
        this.isFormed = formed;
        this.ports.clear();
        this.ports.addAll(newPorts);
        setChanged();

        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);

            for (BlockPos portPos : ports) {
                BlockEntity blockEntity = level.getBlockEntity(portPos);
                if (blockEntity instanceof DatacenterPortBlockEntity portBlockEntity) {
                    portBlockEntity.setControllerPos(formed ? worldPosition : null);
                }
            }
        }
    }
}
