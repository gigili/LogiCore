package dev.gacbl.logicore.blocks.computer;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComputerBlockEntity extends CoreCycleProviderBlockEntity implements MenuProvider {
    public static final int RACK_CAPACITY = 9;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(
                Config.COMPUTER_BASE_CYCLE_GENERATION.get(),
                Config.COMPUTER_CYCLES_PER_PROCESSOR.get(),
                Config.COMPUTER_FE_PER_CYCLE.get(),
                Config.COMPUTER_CYCLE_CAPACITY.get(),
                Config.COMPUTER_FE_CAPACITY.get(),
                Config.COMPUTER_DATACENTER_BOOST.get(),
                ComputerModule.COMPUTER_BLOCK_ENTITY.get(), pos, state
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.computer");
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
            return stack.is(ProcessorUnitModule.PROCESSOR_UNIT.get());
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
