package dev.gacbl.logicore.blocks.serverrack;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.multiblock.AbstractSealedController;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.server.ServerItem;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncCycleDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;

import java.util.Objects;

public class ServerRackBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity, ICycleProvider {
    public static final int RACK_CAPACITY = 9;
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private int serverCacheCount = 0;

    private final int BASE_CYCLE_GENERATION;
    private final int CYCLES_PER_PROCESSOR;
    private final int FE_PER_CYCLE;

    private final EnergyStorage energyStorage;
    private final CycleStorage cycleStorage;

    public boolean isGenerating = false;

    public BlockPos dataCenterController = null;

    private boolean hasDataCenterBoost = false;

    private int dataCenterBoost = 0;

    protected int cachedProcessorCount = 0;


    public ServerRackBlockEntity(BlockPos pos, BlockState state) {
        super(ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(), pos, state);
        BASE_CYCLE_GENERATION = Config.SERVER_RACK_BASE_CYCLE_GENERATION.get();
        CYCLES_PER_PROCESSOR = Config.SERVER_RACK_CYCLES_PER_PROCESSOR.get();
        FE_PER_CYCLE = Config.SERVER_RACK_FE_PER_CYCLE.get();
        energyStorage = new EnergyStorage(Config.SERVER_RACK_FE_CAPACITY.get(), 100_000, 100_000);
        cycleStorage = new CycleStorage(Config.SERVER_RACK_CYCLE_CAPACITY.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<ServerRackBlockEntity> recyclerBlockEntityAnimationState) {
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            BlockEntity entity = level.getBlockEntity(worldPosition);
            if (entity instanceof ServerRackBlockEntity) {
                if (state.getValue(ServerRackBlock.DOOR_OPENING)) {
                    recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("door_open", Animation.LoopType.HOLD_ON_LAST_FRAME));
                } else if (state.getValue(ServerRackBlock.DOOR_CLOSING)) {
                    recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("door_close", Animation.LoopType.HOLD_ON_LAST_FRAME));
                } else {
                    recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("idle", Animation.LoopType.PLAY_ONCE));
                }
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.server_rack");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ServerRackMenu(containerId, playerInventory, this, this.data);
    }

    public int getServerCount() {
        return this.serverCacheCount;
    }

    public int getMaxProcessorCount() {
        return RACK_CAPACITY;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("energy", this.energyStorage.serializeNBT(registries));
        tag.put("cycles", this.cycleStorage.serializeNBT(registries));
        tag.putBoolean("isGenerating", isGenerating);
        if (dataCenterController != null) {
            tag.put("dataCenterController", NbtUtils.writeBlockPos(dataCenterController));
        }
        tag.putBoolean("hasDataCenterBoost", hasDataCenterBoost);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        updateServerCount();
        if (tag.contains("energy", 3) && tag.get("energy") != null) {
            this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("energy")));
        }
        if (tag.contains("cycles", 10)) {
            this.cycleStorage.deserializeNBT(registries, (CompoundTag) tag.get("cycles"));
        }

        if (tag.contains("isGenerating")) {
            isGenerating = tag.getBoolean("isGenerating");
        }

        if (tag.contains("dataCenterController")) {
            dataCenterController = NbtUtils.readBlockPos(tag, "dataCenterController").orElse(null);
        }

        hasDataCenterBoost = tag.getBoolean("hasDataCenterBoost");
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(RACK_CAPACITY) {
        @Override
        protected void onContentsChanged(int slot) {
            updateServerCount();
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ServerItem;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    public void dropContents() {
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(i));
        }
    }

    private void updateServerCount() {
        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        this.serverCacheCount = count;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ServerRackBlockEntity be) {
        if (level.isClientSide) return;
        be.hasDataCenterBoost = false;
        if (be.dataCenterController != null) {
            if (level.getBlockEntity(be.dataCenterController) instanceof AbstractSealedController abc) {
                if (abc.isFormed) {
                    be.hasDataCenterBoost = true;
                }
            }
        }
        be.generateCycles();

        if (state.hasProperty(ServerRackBlock.GENERATING)) {
            boolean currentGeneratingState = state.getValue(ServerRackBlock.GENERATING);

            if (be.isGenerating != currentGeneratingState) {
                level.setBlock(pos, state.setValue(ServerRackBlock.GENERATING, be.isGenerating), 3);
                if (state.hasProperty(ServerRackBlock.HALF)) {
                    updateOtherHalf(level, pos, state, be.isGenerating);
                }
            }
        }
    }

    private static void updateOtherHalf(Level level, BlockPos pos, BlockState state, boolean isWorking) {
        DoubleBlockHalf half = state.getValue(ServerRackBlock.HALF);
        BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();

        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.getBlock() instanceof ServerRackBlock) {
            level.setBlock(otherPos, otherState.setValue(ServerRackBlock.GENERATING, isWorking), 3);
        }
    }

    private void generateCycles() {
        if (this.level == null || this.level.isClientSide) {
            isGenerating = false;
            return;
        }

        if (this.cycleStorage.getCyclesAvailable() >= this.cycleStorage.getCycleCapacity()) {
            isGenerating = false;
            return;

        }
        if (this.energyStorage.getEnergyStored() < FE_PER_CYCLE) {
            isGenerating = false;
            return;
        }

        int processorCount = getServerCount();

        if (processorCount == 0) {
            isGenerating = false;
            return;
        }

        long cyclesToGenerate = calculateBaseCycleGeneration();

        long feCost = Math.min(cyclesToGenerate * FE_PER_CYCLE, 99_999L);
        if (hasDataCenterBoost) {
            cyclesToGenerate += 100;
        }

        boolean prevGenerating = isGenerating;

        if (this.energyStorage.extractEnergy((int) feCost, true) == feCost) {
            isGenerating = true;
            this.energyStorage.extractEnergy((int) feCost, false);
            this.cycleStorage.receiveCycles(cyclesToGenerate, false);
            setChanged();
        } else {
            isGenerating = false;
        }

        if (prevGenerating != isGenerating) {
            syncData();
        }
    }

    public void syncData() {
        if (this.level != null && !this.level.isClientSide) {
            PacketHandler.sendToClientsTrackingChunk(
                    this.level,
                    this.getBlockPos(),
                    new SyncCycleDataPayload(this.worldPosition, this.energyStorage.getEnergyStored(), this.cycleStorage.getCyclesAvailable(), this.isGenerating)
            );
        }
    }

    @Override
    public long getCyclesAvailable() {
        return this.cycleStorage.getCyclesAvailable();
    }

    @Override
    public long getCycleCapacity() {
        return this.cycleStorage.getCycleCapacity();
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        return this.cycleStorage.extractCycles(maxExtract, simulate);
    }

    @Override
    public long receiveCycles(long receive, boolean simulate) {
        return this.cycleStorage.receiveCycles(receive, simulate);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return (int) switch (index) {
                case 0 -> ServerRackBlockEntity.this.energyStorage.getEnergyStored();
                case 1 -> ServerRackBlockEntity.this.energyStorage.getMaxEnergyStored();
                case 2 -> ServerRackBlockEntity.this.cycleStorage.getCyclesAvailable();
                case 3 -> ServerRackBlockEntity.this.cycleStorage.getCycleCapacity();
                case 4 -> ServerRackBlockEntity.this.calculateBaseCycleGeneration();
                case 5 -> ServerRackBlockEntity.this.CYCLES_PER_PROCESSOR;
                case 6 -> ServerRackBlockEntity.this.FE_PER_CYCLE;
                case 7 -> getServerCount();
                case 8 -> ServerRackBlockEntity.this.hasDataCenterBoost ? 1 : 0;
                case 9 -> ServerRackBlockEntity.this.dataCenterBoost;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public int calculateBaseCycleGeneration() {
        long cyclesToGenerate = BASE_CYCLE_GENERATION;
        ItemStackHandler handler = getItemHandler();
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                var stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof ProcessorUnitItem processor) {
                    cyclesToGenerate += processor.tier.cycleRate.get();
                }
            }
        }
        return (int) cyclesToGenerate;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
