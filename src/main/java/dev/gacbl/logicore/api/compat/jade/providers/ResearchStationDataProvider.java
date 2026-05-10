package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ResearchStationDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof ResearchStationBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof ResearchStationBlockEntity be)) return;
        data.putInt(ResearchStationProvider.PROGRESS_KEY, be.getProgress());
        data.putInt(ResearchStationProvider.MAX_PROGRESS_KEY, be.getMaxProgress());
        data.putLong(ResearchStationProvider.STORED_KEY, be.getCyclesStored());
        data.putLong(ResearchStationProvider.DEMAND_KEY, be.getCycleDemand());
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("research_station");
    }
}
