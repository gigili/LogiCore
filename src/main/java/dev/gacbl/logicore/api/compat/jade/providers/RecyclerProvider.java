package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RecyclerProvider implements IBlockComponentProvider {
    INSTANCE;

    static final String PROGRESS_KEY = "RecycleProgress";
    static final String MAX_PROGRESS_KEY = "RecycleMaxProgress";
    static final String STORED_KEY = "StoredCycles";
    static final String CAPACITY_KEY = "CycleCapacity";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        int progress = 0;
        int maxProgress = 0;
        long storedCycles = 0;
        long maxCycles = 0;

        if (accessor.getBlockEntity() instanceof RecyclerBlockEntity be) {
            progress = be.getProgress();
            maxProgress = be.getMaxProgress();
            storedCycles = be.getCyclesAvailable();
            maxCycles = be.getCycleCapacity();
        }

        CompoundTag serverData = accessor.getServerData();
        progress = serverData.getInt(PROGRESS_KEY).orElse(progress);
        maxProgress = serverData.getInt(MAX_PROGRESS_KEY).orElse(maxProgress);
        storedCycles = serverData.getLong(STORED_KEY).orElse(storedCycles);
        maxCycles = serverData.getLong(CAPACITY_KEY).orElse(maxCycles);

        int pct = maxProgress > 0 ? (int) Math.round(progress * 100.0D / maxProgress) : 0;
        tooltip.add(Component.translatable("tooltip.logicore.recycle_progress", pct + "% (" + progress + "/" + maxProgress + ")"));
        tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(storedCycles), Utils.formatValues(maxCycles)));
    }

    @Override
    public Identifier getUid() {
        return LogiCore.identifier("recycler");
    }
}
