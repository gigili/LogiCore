package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum CompilerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof CompilerBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof CompilerBlockEntity be)) return;
        data.putLong(CompilerProvider.STORED_KEY, be.getCurrentCycles());
        data.putLong(CompilerProvider.DEMAND_KEY, be.getCycleDemand());
        data.putInt(CompilerProvider.UPGRADE_COUNT_KEY, be.getUpgradeItemHandler(null).copyToList().get(0).getCount());
        data.putInt(CompilerProvider.UPGRADE_MAX_KEY, 16);
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("compiler");
    }
}
