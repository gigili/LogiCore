package dev.gacbl.logicore.items.processorunit;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProcessorUnitModule {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(LogiCore.MOD_ID);

    public static final DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT = ITEMS.registerItem("processor_unit", ProcessorUnitItem::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
