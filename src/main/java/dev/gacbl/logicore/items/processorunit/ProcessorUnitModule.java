package dev.gacbl.logicore.items.processorunit;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProcessorUnitModule {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT_BASIC = ITEMS.register("processor_unit_basic", () -> new ProcessorUnitItem(ProcessorUnitTier.BASIC));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT_ADVANCE = ITEMS.register("processor_unit_advance", () -> new ProcessorUnitItem(ProcessorUnitTier.ADVANCED));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT_ULTIMATE = ITEMS.register("processor_unit_ultimate", () -> new ProcessorUnitItem(ProcessorUnitTier.ULTIMATE));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
