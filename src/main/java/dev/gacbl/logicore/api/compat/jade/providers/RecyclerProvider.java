package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
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

public class RecyclerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final RecyclerProvider INSTANCE = new RecyclerProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        long capacity = 0;
        long stored = 0;

        if (accessor.getServerData().contains("CyclesCapacity")) {
            capacity = accessor.getServerData().getLong("CyclesCapacity");
        }

        if (accessor.getServerData().contains("CyclesStored")) {
            stored = accessor.getServerData().getLong("CyclesStored");
        }

        if (!accessor.getPlayer().isCrouching()) {
            tooltip.add(Component.translatable("tooltip.logicore.cycles", Utils.formatValues(stored), Utils.formatValues(capacity)));
        } else {
            tooltip.add(Component.translatable("tooltip.logicore.cycles", stored, capacity));
        }

        if (accessor.getServerData().contains("progress") && accessor.getServerData().contains("maxProgress")) {
            int progress = 0;
            int maxProgress = 0;

            if (accessor.getServerData().contains("progress")) {
                progress = accessor.getServerData().getInt("progress");
            }
            if (accessor.getServerData().contains("maxProgress")) {
                maxProgress = accessor.getServerData().getInt("maxProgress");
            }

            if (maxProgress > 0) {
                float fillRatio = (float) progress / (float) maxProgress;
                int percentage = (int) (fillRatio * 100);

                Component text = Component.translatable("tooltip.logicore.recycle_progress", percentage);

                var style = IElementHelper.get().progressStyle()
                        .color(0xFF2196F3, 0xFF0B1F38)
                        .textColor(0xFFFFFFFF);
                tooltip.add(IElementHelper.get().progress(fillRatio, text, style, BoxStyle.getNestedBox(), false));
            }
        }
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getLevel().getBlockEntity(accessor.getPosition());
        return be instanceof RecyclerBlockEntity;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        RecyclerBlockEntity blockEntity = (RecyclerBlockEntity) accessor.getBlockEntity();
        if (blockEntity == null) return;

        data.putInt("progress", blockEntity.getProgress());
        data.putInt("maxProgress", blockEntity.getMaxProgress());
        data.putLong("CyclesCapacity", blockEntity.getCycleCapacity());
        data.putLong("CyclesStored", blockEntity.getCyclesAvailable());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "recycler");
    }
}
