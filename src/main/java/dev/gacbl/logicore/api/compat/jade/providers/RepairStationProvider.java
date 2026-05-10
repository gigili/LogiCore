package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RepairStationProvider implements IBlockComponentProvider {
    INSTANCE;

    static final String PROGRESS_KEY = "RepairProgress";
    static final String MAX_PROGRESS_KEY = "RepairMaxProgress";
    static final String STORED_KEY = "StoredCycles";
    static final String DEMAND_KEY = "CycleDemand";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        int progress = 0;
        int maxProgress = 0;
        long storedCycles = 0;
        long demand = 0;

        if (accessor.getBlockEntity() instanceof RepairStationBlockEntity be) {
            progress = be.getProgress();
            maxProgress = be.getMaxProgress();
            storedCycles = be.getCyclesStored();
            demand = be.getCycleDemand();
        }

        CompoundTag serverData = accessor.getServerData();
        progress = serverData.getInt(PROGRESS_KEY).orElse(progress);
        maxProgress = serverData.getInt(MAX_PROGRESS_KEY).orElse(maxProgress);
        storedCycles = serverData.getLong(STORED_KEY).orElse(storedCycles);
        demand = serverData.getLong(DEMAND_KEY).orElse(demand);

        if (maxProgress > 0) {
            int pct = (int) Math.round(progress * 100.0D / maxProgress);
            tooltip.add(Component.translatable("tooltip.logicore.repair_progress", pct + "% (" + progress + "/" + maxProgress + ")"));
        } else {
            tooltip.add(Component.translatable("tooltip.logicore.no_repair_in_progress"));
        }

        tooltip.add(Component.translatable("tooltip.logicore.cycles_stored", Utils.formatValues(storedCycles)));
        tooltip.add(Component.translatable("tooltip.logicore.cycles_demand", Utils.formatValues(demand)));
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("repair_station");
    }
}
