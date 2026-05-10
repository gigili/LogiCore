package dev.gacbl.logicore.blocks.serverrack;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.server.ServerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ServerRackBlockEntity extends CoreCycleProviderBlockEntity implements MenuProvider, GeoBlockEntity {
    public static final int RACK_CAPACITY = 9;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
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
        controllers.add(new AnimationController<>("controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationTest<ServerRackBlockEntity> state) {
        ServerRackBlockEntity be = state.animatable();
        if (be.getLevel() != null) {
            BlockState blockState = be.getLevel().getBlockState(be.getBlockPos());
            if (blockState.getBlock() instanceof ServerRackBlock) {
                if (blockState.getValue(ServerRackBlock.DOOR_OPENING)) {
                    state.setAndContinue(RawAnimation.begin().then("door_open", LoopType.HOLD_ON_LAST_FRAME));
                } else if (blockState.getValue(ServerRackBlock.DOOR_CLOSING)) {
                    state.setAndContinue(RawAnimation.begin().then("door_close", LoopType.HOLD_ON_LAST_FRAME));
                } else {
                    state.setAndContinue(RawAnimation.begin().then("idle", LoopType.PLAY_ONCE));
                }
            }
        }
        return PlayState.CONTINUE;
    }

    public InteractionResult handleRightClick(Player player, BlockHitResult hit) {
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;

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
        }

        if (player.isShiftKeyDown()) {
            player.openMenu(this, worldPosition);
            return InteractionResult.CONSUME;
        }

        updateDoorStates(state, false, true);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult handleItemClick(ItemStack stack, Player player, BlockHitResult hit) {
        if (level == null) return InteractionResult.PASS;

        if (stack.getItem() instanceof ServerItem) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            BlockState state = level.getBlockState(worldPosition);
            if (!state.getValue(ServerRackBlock.DOOR_OPENING)) {
                handleRightClick(player, hit);
                return InteractionResult.SUCCESS;
            }

            var slots = itemHandler.copyToList();
            for (int slot = 0; slot < slots.size(); slot++) {
                if (slots.get(slot).isEmpty()) {
                    ItemStack toInsert = stack.split(1);
                    itemHandler.set(slot, ItemResource.of(toInsert), 1);
                    level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private void updateDoorStates(BlockState currentState, boolean isOpening, boolean isClosing) {
        if (level == null) return;

        level.setBlock(worldPosition, currentState
                .setValue(ServerRackBlock.DOOR_OPENING, isOpening)
                .setValue(ServerRackBlock.DOOR_CLOSING, isClosing), Block.UPDATE_ALL);

        DoubleBlockHalf half = currentState.getValue(ServerRackBlock.HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? worldPosition.above() : worldPosition.below();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.getBlock() instanceof ServerRackBlock) {
            level.setBlock(otherPos, otherState
                    .setValue(ServerRackBlock.DOOR_OPENING, isOpening)
                    .setValue(ServerRackBlock.DOOR_CLOSING, isClosing), Block.UPDATE_ALL);
        }

        doorTimer = 20;

        BlockEntity otherBe = level.getBlockEntity(otherPos);
        if (otherBe instanceof ServerRackBlockEntity) {
            ((ServerRackBlockEntity) otherBe).doorTimer = 20;
        }
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (doorTimer > 0) {
            doorTimer--;
            if (doorTimer == 0) {
                BlockState state = level.getBlockState(worldPosition);
                if (state.getBlock() instanceof ServerRackBlock) {
                    if (state.getValue(ServerRackBlock.DOOR_CLOSING)) {
                        level.setBlock(worldPosition, state.setValue(ServerRackBlock.DOOR_CLOSING, false), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(RACK_CAPACITY) {
        @Override
        protected void onContentsChanged(int slot, @NonNull ItemStack stack) {
            updateProcessorCountCache();
            setChanged();
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            return resource.toStack().getItem() instanceof ServerItem;
        }

        @Override
        protected int getCapacity(int slot, @NonNull ItemResource resource) {
            return 1;
        }
    };

    @Override
    public ItemStacksResourceHandler getItemHandler() {
        return this.itemHandler;
    }

    public void dropContents() {
        if (this.level == null) return;
        var slots = itemHandler.copyToList();
        for (int i = 0; i < slots.size(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), slots.get(i));
        }
    }

    private void updateProcessorCountCache() {
        int count = 0;
        int currentServerCacheCount = 0;
        var slots = itemHandler.copyToList();
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ServerItem) {
                currentServerCacheCount++;
                ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                for (var innerStack : (Iterable<ItemStack>) contents.nonEmptyItemCopyStream()::iterator) {
                    if (innerStack.getItem() instanceof ProcessorUnitItem) {
                        count++;
                    }
                }
            }
        }
        this.serverCacheCount = currentServerCacheCount;
        this.totalProcessorCount = count;
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
        return totalProcessorCount;
    }

    @Override
    public int getMaxProcessorCount() {
        return RACK_CAPACITY;
    }

    public int getServerCount() {
        return serverCacheCount;
    }
}
