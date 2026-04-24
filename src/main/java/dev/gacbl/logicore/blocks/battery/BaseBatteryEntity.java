package dev.gacbl.logicore.blocks.battery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BaseBatteryEntity extends BlockEntity {
    private final IEnergyStorage energyStorage;
    private int lastSyncedEnergy = -1;

    public BaseBatteryEntity(BlockPos pos, BlockState blockState) {
        super(BatteryModule.BATTERY_BE.get(), pos, blockState);

        if (blockState.getBlock() instanceof BatteryBlock batteryBlock) {
            BatteryTier tier = batteryBlock.getTier();
            if (tier == BatteryTier.CREATIVE) {
                this.energyStorage = new CreativeEnergyStorage();
            } else {
                this.energyStorage = new EnergyStorage(tier.capacity.get(), tier.maxTransfer.get(), tier.maxTransfer.get());
            }
        } else {
            this.energyStorage = new EnergyStorage(1000, 100, 100);
        }
    }

    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.energyStorage instanceof EnergyStorage es) {
            tag.put("energy", es.serializeNBT(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy") && tag.get("energy") != null && this.energyStorage instanceof EnergyStorage es) {
            es.deserializeNBT(registries, Objects.requireNonNull(tag.get("energy")));
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BaseBatteryEntity batteryBlockEntity) {
        if (level.isClientSide) return;

        if (batteryBlockEntity.energyStorage instanceof CreativeEnergyStorage) {
            for (Direction direction : Direction.values()) {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos.relative(direction), direction.getOpposite());
                if (target != null && target.canReceive()) {
                    target.receiveEnergy(Integer.MAX_VALUE, false);
                }
            }
        }

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
