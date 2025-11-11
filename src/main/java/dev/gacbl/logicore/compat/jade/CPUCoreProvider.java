package dev.gacbl.logicore.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.cpucore.CPUCoreBlockEntity;
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
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof CPUCoreBlockEntity cpuCore) {
            data.putLong("Cycles", cpuCore.getCycleStorage().getCyclesAvailable());
            data.putLong("MaxCycles", cpuCore.getCycleStorage().getCycleCapacity());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cpu_core");
    }
}
