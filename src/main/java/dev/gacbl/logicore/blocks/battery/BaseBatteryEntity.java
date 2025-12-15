package dev.gacbl.logicore.blocks.battery;

import dev.gacbl.logicore.blocks.battery.basic.BasicBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class BaseBatteryEntity extends BlockEntity {
    private final EnergyStorage energyStorage;

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

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BasicBatteryBlockEntity basicBatteryBlockEntity) {
        if (level.isClientSide) return;
    }
}
