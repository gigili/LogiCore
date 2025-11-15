package dev.gacbl.logicore.serverrack;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleStorage;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ServerRackBlockEntity extends BlockEntity implements MenuProvider {
    public static final int RACK_CAPACITY = 9;
    // --- Constants ---
    private static final int BASE_CYCLE_GENERATION = Config.SERVER_RACK_BASE_CYCLE_GENERATION.get();
    private static final int CYCLES_PER_PROCESSOR = Config.SERVER_RACK_CYCLES_PER_PROCESSOR.get();
    private static final int FE_PER_CYCLE = Config.SERVER_RACK_FE_PER_CYCLE.get();

    // --- Energy & Cycle Storage ---
    private static final int CYCLE_CAPACITY = Config.SERVER_RACK_CYCLE_CAPACITY.get();
    private static final int FE_CAPACITY = Config.SERVER_RACK_FE_CAPACITY.get();
    private final EnergyStorage energyStorage = new EnergyStorage(FE_CAPACITY);
    private final CycleStorage cycleStorage = new CycleStorage(CYCLE_CAPACITY);

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
        tag.put("energy", this.energyStorage.serializeNBT(registries));
        tag.put("cycles", this.cycleStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.contains("energy", 3) && tag.get("energy") != null) {
            this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("energy")));
        }
        if (tag.contains("cycles", 10)) {
            this.cycleStorage.deserializeNBT(registries, (CompoundTag) tag.get("cycles"));
        }
    }

    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public ICycleStorage getCycleStorage() {
        return this.cycleStorage;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ServerRackBlockEntity be) {
        if (level.isClientSide) return;

        be.generateCycles();
    }

    private void generateCycles() {
        if (this.level == null) return;

        if (this.cycleStorage.getCyclesAvailable() >= this.cycleStorage.getCycleCapacity()) return;
        if (this.energyStorage.getEnergyStored() < FE_PER_CYCLE) return;

        int processorCount = getProcessorCount();

        if (processorCount == 0) return;

        long cyclesToGenerate = BASE_CYCLE_GENERATION + ((long) processorCount * CYCLES_PER_PROCESSOR);
        long feCost = cyclesToGenerate * FE_PER_CYCLE;

        if (this.energyStorage.extractEnergy((int) feCost, true) == feCost) {
            this.energyStorage.extractEnergy((int) feCost, false);
            this.cycleStorage.receiveCycles(cyclesToGenerate, false);
            setChanged();
        }
    }
}
