package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum RecyclerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof RecyclerBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof RecyclerBlockEntity be)) return;
        data.putInt(RecyclerProvider.PROGRESS_KEY, be.getProgress());
        data.putInt(RecyclerProvider.MAX_PROGRESS_KEY, be.getMaxProgress());
        data.putLong(RecyclerProvider.STORED_KEY, be.getCyclesAvailable());
        data.putLong(RecyclerProvider.CAPACITY_KEY, be.getCycleCapacity());
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("recycler");
    }
}
