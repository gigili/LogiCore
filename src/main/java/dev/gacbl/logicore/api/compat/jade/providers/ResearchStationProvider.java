package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ResearchStationProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final ResearchStationProvider INSTANCE = new ResearchStationProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
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
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "research_station");
    }
}
