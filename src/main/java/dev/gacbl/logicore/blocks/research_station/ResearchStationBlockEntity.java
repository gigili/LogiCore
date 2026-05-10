package dev.gacbl.logicore.blocks.research_station;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.blocks.research_station.ui.ResearchStationMenu;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.NotifyResearchCompletePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
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
            ItemStack stack = resource.toStack();
            return CycleValueManager.hasCycleValue(stack) && !ClientKnowledgeData.isUnlocked(Utils.getItemKey(stack));
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return 1;
        }
    };

    private void requestCycles() {
        if (level == null || level.isClientSide() || level.getServer() == null) return;
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

    public ItemStacksResourceHandler getItemHandler() {
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
            return switch (index) {
                case 0 -> ResearchStationBlockEntity.this.progress;
                case 1 -> ResearchStationBlockEntity.this.maxProgress;
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
        if (ownerUUID != null) {
            output.putLong("OwnerMost", ownerUUID.getMostSignificantBits());
            output.putLong("OwnerLeast", ownerUUID.getLeastSignificantBits());
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.child("inventory").ifPresent(itemHandler::deserialize);
        long ownerMost = input.getLongOr("OwnerMost", 0L);
        long ownerLeast = input.getLongOr("OwnerLeast", 0L);
        if (ownerMost != 0L || ownerLeast != 0L) {
            this.ownerUUID = new UUID(ownerMost, ownerLeast);
        }
        progress = input.getIntOr("progress", 0);
        currentCycles = input.getLongOr("currentCycles", 0);
    }

    public void setResearchingState(BlockPos blockPos, BlockState blockState, Boolean isResearching) {
        if (level == null || level.isClientSide()) return;
        level.setBlock(blockPos, blockState.setValue(ResearchStationModule.RESEARCHING, isResearching), 3);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ResearchStationBlockEntity be) {
        if (level.isClientSide()) return;

        ItemStack template = be.itemHandler.copyToList().get(0);
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
                ItemStack resultItem = be.itemHandler.copyToList().get(0);
                String ownerKey = CycleSavedData.getKey((ServerLevel) level, be.getOwner());
                CycleSavedData.get((ServerLevel) level).unlockItem((ServerLevel) level, ownerKey, resultItem);
                if (be.ownerUUID != null) {
                    ServerPlayer player = level.getServer().getPlayerList().getPlayer(be.ownerUUID);
                    if (player != null) {
                        PacketHandler.sendToPlayer(player, new NotifyResearchCompletePayload(resultItem));
                    }
                }
                be.itemHandler.set(0, ItemResource.EMPTY, 0);
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
        Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.copyToList().get(0));
    }

    @Override
    public long getCycleDemand() {
        ItemStack stack = itemHandler.copyToList().get(0);
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
