package dev.gacbl.logicore.blocks.battery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BaseBatteryEntity extends BlockEntity {
    private final EnergyStorage energyStorage;
    private int lastSyncedEnergy = -1;

    public BaseBatteryEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState,
            int maxCapacity,
            int maxInput,
            int maxOutput
    ) {
        super(type, pos, blockState);
        this.energyStorage = new EnergyStorage(maxCapacity, maxInput, maxOutput);
    }

    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", this.energyStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy", 3) && tag.get("energy") != null) {
            this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("energy")));
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BaseBatteryEntity batteryBlockEntity) {
        if (level.isClientSide) return;

        int currentEnergy = batteryBlockEntity.energyStorage.getEnergyStored();
        if (currentEnergy != batteryBlockEntity.lastSyncedEnergy) {
            batteryBlockEntity.lastSyncedEnergy = currentEnergy;
            level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
