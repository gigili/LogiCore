package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DatacenterProvider implements IBlockComponentProvider {
    INSTANCE;

    static final String STORED_KEY = "StoredCycles";
    static final String CAPACITY_KEY = "CycleCapacity";
    static final String CPU_KEY = "CpuCount";
    static final String CPU_MAX_KEY = "CpuMaxCount";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        long stored = 0;
        long capacity = 0;
        long cpuCount = 0;
        long cpuMaxCount = 0;

        if (accessor.getBlockEntity() instanceof DatacenterPortBlockEntity be) {
            stored = be.getCyclesAvailable();
            capacity = be.getCycleCapacity();
            cpuCount = be.getCpuCount();
            cpuMaxCount = be.getCpuMaxCount();
        }

        CompoundTag serverData = accessor.getServerData();
        stored = serverData.getLong(STORED_KEY).orElse(stored);
        capacity = serverData.getLong(CAPACITY_KEY).orElse(capacity);
        cpuCount = serverData.getLong(CPU_KEY).orElse(cpuCount);
        cpuMaxCount = serverData.getLong(CPU_MAX_KEY).orElse(cpuMaxCount);

        tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(stored), Utils.formatValues(capacity)));
        tooltip.add(Component.translatable("tooltip.logicore.cpus", Utils.formatValues(cpuCount), Utils.formatValues(cpuMaxCount)));
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("datacenter_port");
    }
}
