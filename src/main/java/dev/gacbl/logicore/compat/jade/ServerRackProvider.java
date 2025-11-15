package dev.gacbl.logicore.compat.jade;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.serverrack.ServerRackBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ServerRackProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final ServerRackProvider INSTANCE = new ServerRackProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        int count = 0;
        long cycles = 0;
        long max = Config.SERVER_RACK_CYCLE_CAPACITY.get();

        if (accessor.getServerData().contains("Count")) {
            count = accessor.getServerData().getInt("Count");
        }

        if (accessor.getServerData().contains("Cycles") && accessor.getServerData().contains("MaxCycles")) {
            cycles = accessor.getServerData().getLong("Cycles");
            max = accessor.getServerData().getLong("MaxCycles");
        }

        tooltip.add(Component.translatable("tooltip.logicore.cycles", String.format("%,d", cycles), String.format("%,d", max)));
        tooltip.add(Component.translatable("tooltip.logicore.processors", count, ServerRackBlockEntity.RACK_CAPACITY));
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        BlockEntity beBelow = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        return be instanceof ServerRackBlockEntity || beBelow instanceof ServerRackBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        ServerRackBlockEntity rackBlockEntity = (ServerRackBlockEntity) accessor.getBlockEntity();

        if (rackBlockEntity == null) {
            rackBlockEntity = (ServerRackBlockEntity) accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        }

        if (rackBlockEntity == null) return;

        data.putInt("Count", rackBlockEntity.getProcessorCount());
        data.putLong("Cycles", rackBlockEntity.getCycleStorage().getCyclesAvailable());
        data.putLong("MaxCycles", rackBlockEntity.getCycleStorage().getCycleCapacity());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "server_rack");
    }
}
