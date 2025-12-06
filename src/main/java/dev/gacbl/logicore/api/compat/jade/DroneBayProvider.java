package dev.gacbl.logicore.api.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlockEntity;
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

import java.util.Objects;

public class DroneBayProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final DroneBayProvider INSTANCE = new DroneBayProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        long cyclesCapacity = accessor.getServerData().getLong("CyclesCapacity");
        long cyclesAvailable = accessor.getServerData().getLong("CyclesAvailable");
        tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(cyclesAvailable), Utils.formatValues(cyclesCapacity)));
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        return be instanceof DroneBayBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        DroneBayBlockEntity blockEntity = (DroneBayBlockEntity) accessor.getBlockEntity();

        if (blockEntity == null) return;

        data.putLong("CyclesCapacity", Objects.requireNonNull(blockEntity.getCycleStorage()).getCycleCapacity());
        data.putLong("CyclesAvailable", blockEntity.getCyclesStored());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "drone_bay");
    }
}
