package dev.gacbl.logicore.items.processorunit;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProcessorUnitModule {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);

    public static final DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT_BASIC = ITEMS.registerItem("processor_unit_basic", props -> new ProcessorUnitItem(props, ProcessorUnitTier.BASIC));
    public static final DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT_ADVANCE = ITEMS.registerItem("processor_unit_advance", props -> new ProcessorUnitItem(props, ProcessorUnitTier.ADVANCED));
    public static final DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT_ULTIMATE = ITEMS.registerItem("processor_unit_ultimate", props -> new ProcessorUnitItem(props, ProcessorUnitTier.ULTIMATE));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
