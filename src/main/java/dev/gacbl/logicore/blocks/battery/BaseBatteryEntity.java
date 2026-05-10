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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.InfiniteEnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseBatteryEntity extends BlockEntity {
    private final EnergyHandler energyHandler;
    private int lastSyncedEnergy = -1;

    public BaseBatteryEntity(BlockPos pos, BlockState blockState) {
        super(BatteryModule.BATTERY_BE.get(), pos, blockState);

        if (blockState.getBlock() instanceof BatteryBlock batteryBlock) {
            BatteryTier tier = batteryBlock.getTier();
            if (tier == BatteryTier.CREATIVE) {
                this.energyHandler = InfiniteEnergyHandler.INSTANCE;
            } else {
                this.energyHandler = new SimpleEnergyHandler(tier.capacity.get(), tier.maxTransfer.get(), tier.maxTransfer.get());
            }
        } else {
            this.energyHandler = new SimpleEnergyHandler(1000, 100, 100);
        }
    }

    public EnergyHandler getEnergyHandler() {
        return this.energyHandler;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (this.energyHandler instanceof SimpleEnergyHandler es) {
            es.serialize(output.child("energy"));
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        if (this.energyHandler instanceof SimpleEnergyHandler es) {
            input.child("energy").ifPresent(es::deserialize);
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BaseBatteryEntity batteryBlockEntity) {
        if (level.isClientSide()) return;

        if (batteryBlockEntity.energyHandler instanceof InfiniteEnergyHandler) {
            for (Direction direction : Direction.values()) {
                EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, blockPos.relative(direction), direction.getOpposite());
                if (target != null && target.getAmountAsLong() < target.getCapacityAsLong()) {
                    try (Transaction tx = Transaction.openRoot()) {
                        target.insert(Integer.MAX_VALUE, tx);
                        tx.commit();
                    }
                }
            }
        }

        int currentEnergy = batteryBlockEntity.energyHandler.getAmountAsInt();
        if (currentEnergy != batteryBlockEntity.lastSyncedEnergy) {
            batteryBlockEntity.lastSyncedEnergy = currentEnergy;
            level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
