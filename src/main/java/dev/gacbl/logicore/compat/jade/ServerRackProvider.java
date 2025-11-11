package dev.gacbl.logicore.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.cpucore.CPUCoreBlockEntity;
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
        int max = ServerRackBlockEntity.RACK_CAPACITY;;
        if (accessor.getServerData().contains("Count")) {
            count = accessor.getServerData().getInt("Count");
        }

        if(count == 0) {
            BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
            if (be instanceof ServerRackBlockEntity rackBlockEntity) {
                count = rackBlockEntity.getProcessorCount();
            }
        }

        tooltip.add(Component.translatable("tooltip.logicore.processors", count, max));
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        ServerRackBlockEntity rackBlockEntity = (ServerRackBlockEntity) accessor.getBlockEntity();
        data.putInt("Count", rackBlockEntity.getProcessorCount());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "server_rack");
    }
}
