package dev.gacbl.logicore.blocks.serverrack;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.server.ServerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;

public class ServerRackBlockEntity extends CoreCycleProviderBlockEntity implements MenuProvider, GeoBlockEntity {
    public static final int RACK_CAPACITY = 9;
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private int serverCacheCount = 0;
    private int totalProcessorCount = 0;
    private int doorTimer = 0;

    public ServerRackBlockEntity(BlockPos pos, BlockState state) {
        super(
                0,
                0,
                Config.SERVER_RACK_FE_PER_CYCLE.get(),
                Config.SERVER_RACK_CYCLE_CAPACITY.get(),
                Config.SERVER_RACK_FE_CAPACITY.get(),
                Config.SERVER_RACK_DATACENTER_BOOST.get(),
                ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(), pos, state
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<ServerRackBlockEntity> recyclerBlockEntityAnimationState) {
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof ServerRackBlock) {
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

    public InteractionResult handleRightClick(Player player, BlockHitResult hit) {
        if (level == null || level.isClientSide) return InteractionResult.SUCCESS;

        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof ServerRackBlock)) return InteractionResult.PASS;

        boolean isOpening = state.getValue(ServerRackBlock.DOOR_OPENING);

        if (!isOpening) {
            if (player.isShiftKeyDown()) {
                player.openMenu(this, worldPosition);
                return InteractionResult.CONSUME;
            }

            updateDoorStates(state, true, false);
            level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        } else {
            if (!player.isShiftKeyDown()) {
                updateDoorStates(state, false, true);
                level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.openMenu(this, worldPosition);
                return InteractionResult.CONSUME;
            }
        }
    }

    public ItemInteractionResult handleItemClick(ItemStack stack, Player player, BlockHitResult hit) {
        if (level == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof ServerItem) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;

            BlockState state = level.getBlockState(worldPosition);
            if (!state.getValue(ServerRackBlock.DOOR_OPENING)) {
                handleRightClick(player, hit);
                return ItemInteractionResult.SUCCESS;
            }

            ItemStackHandler handler = getItemHandler();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (handler.getStackInSlot(slot).isEmpty()) {
                    handler.setStackInSlot(slot, stack.split(1));
                    level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
                    return ItemInteractionResult.CONSUME;
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void updateDoorStates(BlockState state, boolean opening, boolean closing) {
        if (level == null) return;
        BlockState newState = state.setValue(ServerRackBlock.DOOR_OPENING, opening)
                .setValue(ServerRackBlock.DOOR_CLOSING, closing);
        level.setBlock(worldPosition, newState, Block.UPDATE_ALL);

        BlockPos above = worldPosition.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(state.getBlock()) && aboveState.getValue(ServerRackBlock.HALF) == DoubleBlockHalf.UPPER) {
            level.setBlock(above, aboveState.setValue(ServerRackBlock.DOOR_OPENING, opening)
                    .setValue(ServerRackBlock.DOOR_CLOSING, closing), Block.UPDATE_ALL);
        }
    }

    @Override
    protected boolean canGenerate() {
        if (level == null) return false;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof ServerRackBlock) {
            return !state.getValue(ServerRackBlock.DOOR_OPENING) && !state.getValue(ServerRackBlock.DOOR_CLOSING);
        }
        return super.canGenerate();
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

    @Override
    public int getProcessorCount() {
        return this.totalProcessorCount;
    }

    @Override
    public int getMaxProcessorCount() {
        return RACK_CAPACITY * 9;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(RACK_CAPACITY) {
        @Override
        protected void onContentsChanged(int slot) {
            updateServerCache();
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

    @Override
    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        updateServerCache();
    }

    public void dropContents() {
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(i));
        }
    }

    private void updateServerCache() {
        int servers = 0;
        int processors = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ServerItem) {
                servers++;
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                if (contents != null) {
                    for (ItemStack innerStack : contents.nonEmptyItems()) {
                        if (innerStack.getItem() instanceof ProcessorUnitItem) {
                            processors++;
                        }
                    }
                }
            }
        }
        this.serverCacheCount = servers;
        this.totalProcessorCount = processors;
    }

    @Override
    public int calculateBaseCycleGeneration() {
        long cyclesToGenerate = BASE_CYCLE_GENERATION;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ServerItem) {
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                if (contents != null) {
                    for (ItemStack innerStack : contents.nonEmptyItems()) {
                        if (innerStack.getItem() instanceof ProcessorUnitItem processor) {
                            cyclesToGenerate += CYCLES_PER_PROCESSOR + processor.tier.cycleRate.get();
                        }
                    }
                }
            }
        }

        /* Natural generation: reward populated racks with efficiency boost
        // 1.0 + (serverCount - 1) * 0.05 => 1.4x boost at 9 servers
         double efficiency = 1.0;// + (Math.max(0, serverCacheCount - 1) * 0.05);
         return (int) (cyclesToGenerate * efficiency); */
        return (int) cyclesToGenerate;
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ServerRackBlockEntity be) {
        BlockState currentState = state;
        if (state.getValue(ServerRackBlock.DOOR_CLOSING)) {
            be.doorTimer++;
            if (be.doorTimer >= 20) {
                be.updateDoorStates(state, false, false);
                be.doorTimer = 0;
                currentState = level.getBlockState(pos);
            }
        } else {
            be.doorTimer = 0;
        }
        CoreCycleProviderBlockEntity.serverTick(level, pos, currentState, be);
    }
}
