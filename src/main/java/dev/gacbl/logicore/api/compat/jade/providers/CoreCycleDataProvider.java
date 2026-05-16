package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum CoreCycleDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return CoreCycleProvider.resolveProvider(accessor) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        CoreCycleProviderBlockEntity be = CoreCycleProvider.resolveProvider(accessor);
        if (be == null) return;
        data.putLong(CoreCycleProvider.STORED_KEY, be.getCyclesAvailable());  
        data.putLong(CoreCycleProvider.CAPACITY_KEY, be.getCycleCapacity());
        data.putInt(CoreCycleProvider.PROCESSOR_COUNT_KEY, be.getProcessorCount());
        data.putInt(CoreCycleProvider.PROCESSOR_MAX_KEY, be.getMaxProcessorCount());
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("core_cycle");
    }
}
