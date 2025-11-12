package dev.gacbl.logicore.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.cpucore.CPUCoreBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CPUCoreProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final CPUCoreProvider INSTANCE = new CPUCoreProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("Cycles") && accessor.getServerData().contains("MaxCycles")) {
            long cycles = accessor.getServerData().getLong("Cycles");
            long max = accessor.getServerData().getLong("MaxCycles");
            tooltip.add(Component.translatable("tooltip.logicore.cycles", String.format("%,d", cycles), String.format("%,d", max)));
        }

        if (accessor.getServerData().contains("ConnectedRacks")) {
            int connected = accessor.getServerData().getInt("ConnectedRacks");
            tooltip.add(Component.translatable("tooltip.logicore.connected_racks", String.format("%,d", connected), String.format("%,d", CPUCoreBlockEntity.MAX_RACKS)));
        }

        if (accessor.getServerData().contains("MaxRacksReached") && accessor.getServerData().getBoolean("MaxRacksReached")) {
            Component msg = Component.translatable("tooltip.logicore.max_racks_reached", CPUCoreBlockEntity.MAX_RACKS);
            tooltip.add(msg.copy().setStyle(msg.getStyle().withColor(ChatFormatting.RED)));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof CPUCoreBlockEntity cpuCore) {
            data.putLong("Cycles", cpuCore.getCycleStorage().getCyclesAvailable());
            data.putLong("MaxCycles", cpuCore.getCycleStorage().getCycleCapacity());
            data.putBoolean("MaxRacksReached", cpuCore.getConnectedRacks().size() > CPUCoreBlockEntity.MAX_RACKS);
            data.putInt("ConnectedRacks", cpuCore.getConnectedRacks().size());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cpu_core");
    }
}
