package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class ResearchStationProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final ResearchStationProvider INSTANCE = new ResearchStationProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        long cycles = 0;
        long cyclesStored = 0;

        if (accessor.getServerData().contains("CyclesDemand")) {
            cycles = accessor.getServerData().getLong("CyclesDemand");
        }

        if (accessor.getServerData().contains("CyclesStored")) {
            cyclesStored = accessor.getServerData().getLong("CyclesStored");
        }

        tooltip.add(Component.translatable("tooltip.logicore.cycles_stored", Utils.formatValues(cyclesStored)));
        tooltip.add(Component.translatable("tooltip.logicore.cycles_demand", Utils.formatValues(cycles)));
        if (accessor.getServerData().contains("progress") && accessor.getServerData().contains("maxProgress")) {
            long progress = accessor.getServerData().getLong("progress");
            long maxProgress = accessor.getServerData().getLong("maxProgress");

            if (maxProgress > 0) {
                float pgr = (float) progress / maxProgress;

                Component text = Component.translatable("tooltip.logicore.research_progress",
                        Utils.formatValues(progress),
                        Utils.formatValues(maxProgress));

                var style = IElementHelper.get().progressStyle()
                        .color(0xFF2196F3, 0xFF0B1F38)
                        .textColor(0xFFFFFFFF);

                tooltip.add(IElementHelper.get().progress(pgr, text, style, BoxStyle.getNestedBox(), false));
                return;
            }
        }

        tooltip.add(Component.literal("No research in progress"));
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        return be instanceof ResearchStationBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        ResearchStationBlockEntity blockEntity = (ResearchStationBlockEntity) accessor.getBlockEntity();
        if (blockEntity == null) return;

        data.putInt("progress", blockEntity.getProgress());
        data.putInt("maxProgress", blockEntity.getMaxProgress());
        data.putLong("CyclesDemand", blockEntity.getCycleDemand());
        data.putLong("CyclesStored", blockEntity.getCyclesStored());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "research_station");
    }
}
