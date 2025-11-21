package dev.gacbl.logicore.blocks.datacable;

import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncCableDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class DataCableBlockEntity extends BlockEntity {
    private UUID NETWORK_UUID = null;

    public DataCableBlockEntity(BlockPos pos, BlockState blockState) {
        this(DataCableModule.DATA_CABLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public DataCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && NETWORK_UUID == null) {
            NetworkManager.get((ServerLevel) this.level).onCablePlaced(this.level, this.worldPosition);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (NETWORK_UUID != null) {
            tag.putUUID("network_uuid", NETWORK_UUID);
        } else if (tag.contains("network_uuid")) {
            tag.remove("network_uuid");
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("network_uuid")) {
            NETWORK_UUID = tag.getUUID("network_uuid");
        }
    }

    public UUID getNetworkUUID() {
        return NETWORK_UUID;
    }

    public void setNetworkUUID(UUID networkUUID) {
        NETWORK_UUID = networkUUID;
        setChanged();
        syncData();
    }

    public void setClientNetworkUUID(UUID uuid) {
        this.NETWORK_UUID = uuid;
    }

    public void syncData() {
        if (level != null && !level.isClientSide) {
            PacketHandler.sendToClientsTrackingChunk(level, this.worldPosition, new SyncCableDataPayload(this.worldPosition, Optional.ofNullable(this.NETWORK_UUID)));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
