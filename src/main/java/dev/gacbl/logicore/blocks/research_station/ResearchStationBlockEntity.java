package dev.gacbl.logicore.blocks.research_station;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.blocks.research_station.ui.ResearchStationMenu;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.NotifyResearchCompletePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ResearchStationBlockEntity extends BlockEntity implements MenuProvider, ICycleConsumer {
    private long currentCycles = 0;
    private int progress = 0;
    private int maxProgress = Config.RS_MAX_RESEARCH_PROGRESS.get();
    private static final int CYCLES_PROCESSED_PER_TICK = Config.RS_CYCLES_PROCESSED_PER_TICK.get();
    private UUID ownerUUID;

    public ResearchStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ResearchStationModule.RESEARCH_STATION_BE.get(), pos, blockState);
    }

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
        setChanged();
    }

    public UUID getOwner() {
        return ownerUUID;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!this.getStackInSlot(0).isEmpty()) {
                requestCycles();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            ResourceLocation itemRes = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return CycleValueManager.hasCycleValue(stack) && !ClientKnowledgeData.isUnlocked(itemRes.toString());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private void requestCycles() {
        if (level == null || level.isClientSide || level.getServer() == null) return;
        NetworkManager manager = NetworkManager.get(level.getServer().overworld());
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof DataCableBlockEntity dc) {
                UUID networkID = dc.getNetworkUUID();
                if (networkID != null && manager.getNetworks().containsKey(networkID)) {
                    manager.getNetworks().get(networkID).requestCycles(getCycleDemand());
                }
            }
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.research_station");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new ResearchStationMenu(containerId, inventory, this, this.data);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putLong("currentCycles", currentCycles);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        progress = tag.getInt("progress");
        currentCycles = tag.getLong("currentCycles");
    }

    public void setResearchingState(BlockPos blockPos, BlockState blockState, Boolean isResearching) {
        if (level == null || level.isClientSide) return;
        level.setBlock(blockPos, blockState.setValue(ResearchStationModule.RESEARCHING, isResearching), 3);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ResearchStationBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack template = be.itemHandler.getStackInSlot(0);
        if (template.isEmpty() || !CycleValueManager.hasCycleValue(template)) {
            if (be.progress > 0) {
                be.progress = 0;
                be.maxProgress = 20;
                be.setResearchingState(blockPos, blockState, false);
            }
            return;
        }

        long cost = be.getCycleDemand();

        int rawDuration = (int) Math.ceil((double) cost / CYCLES_PROCESSED_PER_TICK);

        int calculatedDuration = Math.max(20, Math.min(Config.RS_MAX_TICK_DURATION.get(), rawDuration));

        if (be.maxProgress != calculatedDuration) {
            be.maxProgress = calculatedDuration;
            be.setChanged();
        }

        if (be.progress == 0) {
            be.setResearchingState(blockPos, blockState, false);
        }

        if (be.currentCycles >= cost) {
            be.progress++;
            be.setResearchingState(blockPos, blockState, true);

            if (be.progress >= be.maxProgress) {
                be.currentCycles -= cost;
                be.progress = 0;
                be.setResearchingState(blockPos, blockState, false);
                ItemStack resultItem = be.itemHandler.getStackInSlot(0);
                String ownerKey = CycleSavedData.getKey((ServerLevel) level, be.getOwner());
                ResourceLocation itemRes = BuiltInRegistries.ITEM.getKey(resultItem.getItem());
                CycleSavedData.get((ServerLevel) level).unlockItem((ServerLevel) level, ownerKey, itemRes);
                if (be.ownerUUID != null) {
                    ServerPlayer player = level.getServer().getPlayerList().getPlayer(be.ownerUUID);
                    if (player != null) {
                        PacketHandler.sendToPlayer(player, new NotifyResearchCompletePayload(resultItem));
                    }
                }
                be.itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                be.setResearchingState(blockPos, blockState, false);
            }
            be.setChanged();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.setResearchingState(blockPos, blockState, false);
                be.setChanged();
            }
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
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(0));
    }

    @Override
    public long getCycleDemand() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) return 0;
        return CycleValueManager.getCycleValue(stack) + 1000L;
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        long cap = getCycleDemand() + 1000L;
        long space = cap - currentCycles;
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
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }
}
