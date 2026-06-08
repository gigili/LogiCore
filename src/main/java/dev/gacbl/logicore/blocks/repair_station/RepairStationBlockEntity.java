package dev.gacbl.logicore.blocks.repair_station;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.blocks.repair_station.ui.RepairStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RepairStationBlockEntity extends BlockEntity implements MenuProvider, ICycleConsumer {
    private long currentCycles = 0;
    private int progress = 0;
    private final int baseRepairCost = Config.RP_BASE_REPAIR_COST.get();
    private int requestCooldown = 0;

    public RepairStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(RepairStationModule.REPAIR_STATION_BE.get(), pos, blockState);
    }

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            setChanged();
            if (!stack.isEmpty()) {
                requestCycles();
            }
        }

        @Override
        public boolean isValid(int slot, @NotNull ItemResource resource) {
            if (resource == null || resource.isEmpty()) return false;
            return resource.toStack().isDamaged();
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return 1;
        }
    };

    private void requestCycles() {
        if (level == null || level.isClientSide() || level.getServer() == null) return;

        long demand = getCycleDemand();
        if (demand <= 0) return;

        NetworkManager manager = NetworkManager.get(level.getServer().overworld());
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof DataCableBlockEntity dc) {
                UUID networkID = dc.getNetworkUUID();
                if (networkID != null && manager.getNetworks().containsKey(networkID)) {
                    manager.getNetworks().get(networkID).requestCycles(demand);
                }
            }
        }
    }

    public ItemStacksResourceHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.repair_station");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new RepairStationMenu(containerId, inventory, this, this.data);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> RepairStationBlockEntity.this.progress;
                case 1 -> RepairStationBlockEntity.this.getMaxProgress();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output.child("inventory"));
        output.putInt("progress", progress);
        output.putLong("currentCycles", currentCycles);
        output.putInt("requestCooldown", requestCooldown);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.child("inventory").ifPresent(itemHandler::deserialize);
        progress = input.getIntOr("progress", 0);
        currentCycles = input.getLongOr("currentCycles", 0);
        requestCooldown = input.getIntOr("requestCooldown", 0);
    }

    public void setRepairing(BlockPos blockPos, BlockState blockState, Boolean isRepairing) {
        if (level == null || level.isClientSide()) return;
        level.setBlock(blockPos, blockState.setValue(RepairStationModule.REPAIRING, isRepairing), 3);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, RepairStationBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.requestCooldown > 0) {
            be.requestCooldown--;
        }

        ItemStack stack = be.itemHandler.copyToList().get(0);
        if (stack.isEmpty() || !stack.isDamaged()) {
            if (blockState.getValue(RepairStationModule.REPAIRING)) {
                be.setRepairing(blockPos, blockState, false);
            }
            return;
        }

        long costPerTick = be.baseRepairCost;
        if (be.currentCycles >= costPerTick) {
            be.currentCycles -= costPerTick;
            ItemStack newStack = stack.copy();
            newStack.setDamageValue(newStack.getDamageValue() - 1);
            be.itemHandler.set(0, ItemResource.of(newStack), newStack.getCount());
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            if (!blockState.getValue(RepairStationModule.REPAIRING)) {
                be.setRepairing(blockPos, blockState, true);
            }
            be.setChanged();
        } else {
            if (blockState.getValue(RepairStationModule.REPAIRING)) {
                be.setRepairing(blockPos, blockState, false);
            }
            be.requestCycles();
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void dropContents() {
        if (this.level == null) return;
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.copyToList().get(0));
    }


    @Override
    public long getCycleDemand() {
        ItemStack stack = itemHandler.copyToList().get(0);
        if (stack.isEmpty()) return 0;
        return ((long) stack.getDamageValue() * baseRepairCost);
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        long demand = getCycleDemand();
        long space = demand - currentCycles;
        if (space <= 0) return 0;
        long accepted = Math.min(maxReceive, space);

        if (!simulate && accepted > 0) {
            this.currentCycles += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public long extractCycles(long maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public long getCyclesStored() {
        return this.currentCycles;
    }

    public int getProgress() {
        ItemStack stack = itemHandler.copyToList().get(0);
        if (stack.isEmpty() || !stack.isDamaged()) return 0;
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public int getMaxProgress() {
        ItemStack stack = itemHandler.copyToList().get(0);
        if (stack.isEmpty() || !stack.isDamaged()) return 0;
        return stack.getMaxDamage();
    }
}
