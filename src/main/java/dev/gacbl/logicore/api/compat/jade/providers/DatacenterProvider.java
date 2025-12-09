package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
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

public class DatacenterProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final DatacenterProvider INSTANCE = new DatacenterProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        long cyclesCapacity = accessor.getServerData().getLong("CyclesCapacity");
        long cyclesAvailable = accessor.getServerData().getLong("CyclesAvailable");
        tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(cyclesAvailable), Utils.formatValues(cyclesCapacity)));
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        return be instanceof DatacenterPortBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        DatacenterPortBlockEntity blockEntity = (DatacenterPortBlockEntity) accessor.getBlockEntity();

        if (blockEntity == null) return;

        data.putLong("CyclesCapacity", blockEntity.getCycleCapacity());
        data.putLong("CyclesAvailable", blockEntity.getCyclesAvailable());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "datacenter_port");
    }
}
