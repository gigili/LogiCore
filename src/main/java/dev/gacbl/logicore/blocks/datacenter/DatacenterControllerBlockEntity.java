package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.api.multiblock.MultiblockValidationException;
import dev.gacbl.logicore.api.multiblock.MultiblockValidator;
import dev.gacbl.logicore.blocks.datacenter.ui.DatacenterControllerMenu;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.core.ModTags;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.server.ServerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static dev.gacbl.logicore.blocks.serverrack.ServerRackBlock.HALF;

public class DatacenterControllerBlockEntity extends AbstractSealedController implements MenuProvider {
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
        if (level == null || level.isClientSide()) return;

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
            if (ports.isEmpty()) return;
            for (BlockPos port : ports) {
                if (level.getBlockEntity(port) instanceof DatacenterPortBlockEntity dc) {
                    dc.setControllerPos(worldPosition);
                }
            }
        }
    }

    @Override
    protected void onStructureBroken() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(DatacenterControllerBlock.FORMED, false), 3);
            if (ports.isEmpty()) return;
            for (BlockPos port : ports) {
                if (level.getBlockEntity(port) instanceof DatacenterPortBlockEntity dc) {
                    dc.setControllerPos(null);
                }
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(@NotNull net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);

        output.putBoolean("Formed", isFormed);

        java.util.List<Long> portsList = new java.util.ArrayList<>();
        for (BlockPos portPos : ports) portsList.add(portPos.asLong());
        output.store("Ports", com.mojang.serialization.Codec.LONG.listOf(), portsList);

        java.util.List<Long> providersList = new java.util.ArrayList<>();
        for (BlockPos provider : interiorProviders) providersList.add(provider.asLong());
        output.store("Providers", com.mojang.serialization.Codec.LONG.listOf(), providersList);

        itemHandler.serialize(output.child("inventory"));
    }

    @Override
    public void loadAdditional(@NotNull net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);

        isFormed = input.getBooleanOr("Formed", false);
        ports.clear();
        interiorProviders.clear();

        input.read("Ports", com.mojang.serialization.Codec.LONG.listOf()).ifPresent(list -> {
            for (long v : list) ports.add(BlockPos.of(v));
        });
        input.read("Providers", com.mojang.serialization.Codec.LONG.listOf()).ifPresent(list -> {
            for (long v : list) interiorProviders.add(BlockPos.of(v));
        });

        input.child("inventory").ifPresent(itemHandler::deserialize);
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (level.getGameTime() % 100 == 0) {
            this.cacheDirty = true;
            validateCacheIfNeeded();
        }
        if (!itemHandler.copyToList().get(0).isEmpty()) {
            this.distributeProcessorsAndServers();
        }
    }

    private void validateCacheIfNeeded() {
        if (!cacheDirty || level == null || !isFormed) return;

        interiorProviders.clear();
        ports.clear();

        BlockPos.betweenClosedStream(minPos, maxPos).forEach(pos -> {
            if (level.getBlockEntity(pos) instanceof DatacenterPortBlockEntity) {
                ports.add(pos.immutable());
            }
        });

        cacheDirty = false;
    }

    private void distributeProcessorsAndServers() {
        if (level == null || level.isClientSide()) return;

        BlockPos.betweenClosedStream(minPos, maxPos).forEach(pos -> {
            if (level.getBlockEntity(pos) instanceof CoreCycleProviderBlockEntity providerEntity) {
                if (level.getBlockEntity(pos) instanceof ServerRackBlockEntity srv) {
                    pos = srv.getBlockState().getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
                }

                ItemStacksResourceHandler handler = providerEntity.getItemHandler();
                if (handler != null && providerEntity.getProcessorCount() < providerEntity.getMaxProcessorCount()) {
                    var slots = handler.copyToList();
                    for (int index = 0; index < slots.size(); index++) {
                        ItemStack currentUpgrade = itemHandler.copyToList().get(0);
                        if (slots.get(index).isEmpty() && !currentUpgrade.isEmpty() && handler.isValid(0, ItemResource.of(currentUpgrade))) {
                            try (net.neoforged.neoforge.transfer.transaction.Transaction tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                                handler.insert(index, ItemResource.of(currentUpgrade), 1, tx);
                                tx.commit();
                            }
                            // Also extract from our own inventory
                            try (net.neoforged.neoforge.transfer.transaction.Transaction tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                                itemHandler.extract(0, ItemResource.of(currentUpgrade), 1, tx);
                                tx.commit();
                            }
                        }
                    }
                }
                interiorProviders.add(pos.immutable());
            }
        });
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
        if (level != null && !level.isClientSide()) {
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

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            setChanged();
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            ItemStack stack = resource.toStack();
            return stack.getItem() instanceof ProcessorUnitItem || stack.getItem() instanceof ServerItem;
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return 64;
        }
    };

    public ItemStacksResourceHandler getItemHandler() {
        return itemHandler;
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.copyToList().get(0));
    }

    @Override
    public void setRemoved() {
        if (this.level != null && !this.level.isClientSide()) {
            BlockState stateAtPos = this.level.getBlockState(this.worldPosition);
            if (!stateAtPos.is(this.getBlockState().getBlock())) {
                dropContents();
            }
        }
        super.setRemoved();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.datacenter_controller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new DatacenterControllerMenu(containerId, inventory, this, data);
    }

    public ContainerData data = new SimpleContainerData(1);
}
