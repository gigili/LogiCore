package dev.gacbl.logicore.blocks.serverrack;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    public ServerRackBlockEntity(BlockPos pos, BlockState state) {
        super(
                Config.SERVER_RACK_BASE_CYCLE_GENERATION.get(),
                Config.SERVER_RACK_CYCLES_PER_PROCESSOR.get(),
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

    @Override
    public int getProcessorCount() {
        return this.cachedProcessorCount;
    }

    @Override
    public int getMaxProcessorCount() {
        return RACK_CAPACITY;
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
        updateProcessorCountCache();
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(RACK_CAPACITY) {
        @Override
        protected void onContentsChanged(int slot) {
            updateProcessorCountCache();
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ProcessorUnitItem;
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

    public void dropContents() {
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(i));
        }
    }

    private void updateProcessorCountCache() {
        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        this.cachedProcessorCount = count;
    }
}
