package dev.gacbl.logicore.serverrack;

import dev.gacbl.logicore.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.serverrack.ui.ServerRackMenu;
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

public class ServerRackBlockEntity extends BlockEntity implements MenuProvider {
    public static final int RACK_CAPACITY = 9;

    private BlockPos controllerPos = null;
    private final ItemStackHandler itemHandler = new ItemStackHandler(RACK_CAPACITY) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ProcessorUnitModule.PROCESSOR_UNIT.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.server_rack");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ServerRackMenu(containerId, playerInventory, this);
    }

    public boolean stillValid(Player player) {
        return this.level != null &&
                this.level.getBlockEntity(this.worldPosition) == this &&
                player.distanceToSqr(this.worldPosition.getCenter()) <= 64.0;
    }

    public ServerRackBlockEntity(BlockPos pos, BlockState state) {
        super(ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    public int getProcessorCount() {
        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public void setControllerPos(BlockPos pos) {
        this.controllerPos = pos;
        setChanged();
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public void dropContents() {
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        if (controllerPos != null) {
            tag.putLong("controllerPos", controllerPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.contains("controllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("controllerPos"));
        }
    }
}
