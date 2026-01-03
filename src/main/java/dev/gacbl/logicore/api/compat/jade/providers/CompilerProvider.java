package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
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

public class CompilerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final CompilerProvider INSTANCE = new CompilerProvider();

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

        if (accessor.getServerData().contains("UpgradesCount")) {
            int cnt = accessor.getServerData().getInt("UpgradesCount");
            int cntMax = accessor.getServerData().getInt("UpgradesCountMax");
            tooltip.add(Component.translatable("tooltip.logicore.compiler.stack_upgrades_max", cnt, cntMax));
        }
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        return be instanceof CompilerBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        CompilerBlockEntity blockEntity = (CompilerBlockEntity) accessor.getBlockEntity();

        if (blockEntity == null) return;

        if (blockEntity.getCycleStorage() != null) {
            data.putLong("CyclesDemand", blockEntity.getCycleStorage().getCycleDemand());
            data.putLong("CyclesStored", blockEntity.getCurrentCycles());
            data.putLong("UpgradesCount", blockEntity.getUpgradeItemHandler(null).getStackInSlot(0).getCount());
            data.putInt("UpgradesCountMax", blockEntity.getUpgradeItemHandler(null).getSlotLimit(0));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "compiler");
    }
}
