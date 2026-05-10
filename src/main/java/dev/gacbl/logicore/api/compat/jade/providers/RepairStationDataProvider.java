package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum RepairStationDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof RepairStationBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof RepairStationBlockEntity be)) return;
        data.putInt(RepairStationProvider.PROGRESS_KEY, be.getProgress());
        data.putInt(RepairStationProvider.MAX_PROGRESS_KEY, be.getMaxProgress());
        data.putLong(RepairStationProvider.STORED_KEY, be.getCyclesStored());
        data.putLong(RepairStationProvider.DEMAND_KEY, be.getCycleDemand());
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("repair_station");
    }
}
