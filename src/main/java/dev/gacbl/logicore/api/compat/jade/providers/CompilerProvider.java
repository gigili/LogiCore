package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CompilerProvider implements IBlockComponentProvider {
    INSTANCE;

    static final String STORED_KEY = "StoredCycles";
    static final String DEMAND_KEY = "CycleDemand";
    static final String UPGRADE_COUNT_KEY = "UpgradeCount";
    static final String UPGRADE_MAX_KEY = "UpgradeMaxCount";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        long stored = 0;
        long demand = 0;
        int upgradeCount = 0;
        int upgradeMax = 16;

        if (accessor.getBlockEntity() instanceof CompilerBlockEntity be) {
            stored = be.getCurrentCycles();
            demand = be.getCycleDemand();
            upgradeCount = be.getUpgradeItemHandler(null).copyToList().get(0).getCount();
        }

        CompoundTag serverData = accessor.getServerData();
        stored = serverData.getLong(STORED_KEY).orElse(stored);
        demand = serverData.getLong(DEMAND_KEY).orElse(demand);
        upgradeCount = serverData.getInt(UPGRADE_COUNT_KEY).orElse(upgradeCount);
        upgradeMax = serverData.getInt(UPGRADE_MAX_KEY).orElse(upgradeMax);

        tooltip.add(Component.translatable("tooltip.logicore.cycles_stored", Utils.formatValues(stored)));
        tooltip.add(Component.translatable("tooltip.logicore.cycles_demand", Utils.formatValues(demand)));
        tooltip.add(Component.translatable("tooltip.logicore.compiler.stack_upgrades_max", upgradeCount, upgradeMax));
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("compiler");
    }
}
