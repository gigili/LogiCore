package dev.gacbl.logicore.blocks.computer;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComputerBlockEntity extends CoreCycleProviderBlockEntity implements MenuProvider {
    public static final int COMPUTER_CPU_CAPACITY = 9;

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
    public int getMaxProcessorCount() {
        return COMPUTER_CPU_CAPACITY;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output.child("inventory"));
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.child("inventory").ifPresent(itemHandler::deserialize);
        updateProcessorCountCache();
    }

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(COMPUTER_CPU_CAPACITY) {
        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            updateProcessorCountCache();
            setChanged();
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            ItemStack stack = resource.toStack();
            if (stack.getItem() instanceof ProcessorUnitItem p) {
                return p.tier == ProcessorUnitTier.BASIC || p.tier == ProcessorUnitTier.ADVANCED;
            }
            return false;
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
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
        var slots = itemHandler.copyToList();
        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).isEmpty()) {
                count++;
            }
        }
        this.cachedProcessorCount = count;
    }
}
