package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum DatacenterDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof DatacenterPortBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof DatacenterPortBlockEntity be)) return;
        data.putLong(DatacenterProvider.STORED_KEY, be.getCyclesAvailable());
        data.putLong(DatacenterProvider.CAPACITY_KEY, be.getCycleCapacity());
        data.putLong(DatacenterProvider.CPU_KEY, be.getCpuCount());
        data.putLong(DatacenterProvider.CPU_MAX_KEY, be.getCpuMaxCount());
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("datacenter_port");
    }
}
