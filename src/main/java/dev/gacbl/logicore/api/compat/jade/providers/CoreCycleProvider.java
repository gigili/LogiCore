package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CoreCycleProvider implements IBlockComponentProvider {
    INSTANCE;

    static final String STORED_KEY = "StoredCycles";
    static final String CAPACITY_KEY = "CycleCapacity";
    static final String PROCESSOR_COUNT_KEY = "ProcessorCount";
    static final String PROCESSOR_MAX_KEY = "ProcessorMaxCount";

    static CoreCycleProviderBlockEntity resolveProvider(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof CoreCycleProviderBlockEntity provider) {
            return provider;
        }
        BlockPos pos = accessor.getPosition();
        if (accessor.getLevel().getBlockEntity(pos.below()) instanceof CoreCycleProviderBlockEntity provider) {
            return provider;
        }
        if (accessor.getLevel().getBlockEntity(pos.above()) instanceof CoreCycleProviderBlockEntity provider) {
            return provider;
        }
        return null;
    }

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, @NonNull BlockAccessor accessor, @NonNull IPluginConfig config) {
        long stored = 0;
        long capacity = 0;
        int processorCount = 0;
        int processorMaxCount = 0;

        CoreCycleProviderBlockEntity be = resolveProvider(accessor);
        if (be != null) {
            stored = be.getCyclesAvailable();
            capacity = be.getCycleCapacity();
            processorCount = be.getProcessorCount();
            processorMaxCount = be.getMaxProcessorCount();
        }

        CompoundTag serverData = accessor.getServerData();
        stored = serverData.getLong(STORED_KEY).orElse(stored);
        capacity = serverData.getLong(CAPACITY_KEY).orElse(capacity);
        processorCount = serverData.getInt(PROCESSOR_COUNT_KEY).orElse(processorCount);
        processorMaxCount = serverData.getInt(PROCESSOR_MAX_KEY).orElse(processorMaxCount);

        tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(stored), Utils.formatValues(capacity)));
        tooltip.add(Component.translatable("tooltip.logicore.processors", processorCount, processorMaxCount));
    }

    @Override
    public @NonNull Identifier getUid() {
        return LogiCore.identifier("core_cycle");
    }
}
