package dev.gacbl.logicore.api.compat.jade.providers;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class DroneEntityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    public static final DroneEntityProvider INSTANCE = new DroneEntityProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("CyclesStored") && accessor.getServerData().contains("CyclesCapacity")) {
            long stored = accessor.getServerData().getLong("CyclesStored");
            long capacity = accessor.getServerData().getLong("CyclesCapacity");

            if (capacity > 0) {
                float progress = (float) stored / capacity;

                Component text = Component.translatable("tooltip.logicore.cycles",
                        Utils.formatValues(stored),
                        Utils.formatValues(capacity));

                var style = IElementHelper.get().progressStyle()
                        .color(0xFF2196F3, 0xFF0B1F38)
                        .textColor(0xFFFFFFFF);

                tooltip.add(IElementHelper.get().progress(progress, text, style, BoxStyle.getNestedBox(), false));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof DroneEntity drone) {
            data.putLong("CyclesStored", drone.getCyclesStored());
            data.putLong("CyclesCapacity", drone.getCycleStorage().getCycleCapacity());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "drone_entity");
    }
}
