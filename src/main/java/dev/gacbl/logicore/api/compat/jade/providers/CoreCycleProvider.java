package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CoreCycleProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final CoreCycleProvider INSTANCE = new CoreCycleProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        int count = 0;
        long cycles = 0;
        long max = 0;

        if (accessor.getServerData().contains("Count")) {
            count = accessor.getServerData().getInt("Count");
        }

        if (accessor.getServerData().contains("Cycles") && accessor.getServerData().contains("MaxCycles")) {
            cycles = accessor.getServerData().getLong("Cycles");
            max = accessor.getServerData().getLong("MaxCycles");
        }

        if (!accessor.getPlayer().isCrouching()) {
            tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(cycles), Utils.formatValues(max)));
        } else {
            tooltip.add(Component.translatable("tooltip.logicore.cycles", String.format("%,d", cycles), String.format("%,d", max)));
        }
        tooltip.add(Component.translatable("tooltip.logicore.processors", count, ServerRackBlockEntity.RACK_CAPACITY * 9));
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        BlockEntity beBelow = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        return be instanceof CoreCycleProviderBlockEntity || beBelow instanceof CoreCycleProviderBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        CoreCycleProviderBlockEntity blockEntity = (CoreCycleProviderBlockEntity) accessor.getBlockEntity();
        if (accessor.getBlock() instanceof ServerRackBlock) {
            BlockState state = accessor.getBlockState();
            if (state.getValue(ServerRackBlock.HALF) == DoubleBlockHalf.LOWER) {
                blockEntity = (CoreCycleProviderBlockEntity) accessor.getBlockEntity();
            } else {
                blockEntity = (CoreCycleProviderBlockEntity) accessor.getLevel().getBlockEntity(accessor.getPosition().below());
            }
        }

        if (blockEntity == null) {
            blockEntity = (CoreCycleProviderBlockEntity) accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        }

        if (blockEntity == null) return;

        data.putInt("Count", blockEntity.getProcessorCount());
        data.putLong("Cycles", blockEntity.getCycleStorage().getCyclesAvailable());
        data.putLong("MaxCycles", blockEntity.getCycleStorage().getCycleCapacity());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "server_rack");
    }
}
