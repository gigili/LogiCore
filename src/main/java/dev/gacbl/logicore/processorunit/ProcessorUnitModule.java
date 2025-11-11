package dev.gacbl.logicore.processorunit;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProcessorUnitModule {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, ProcessorUnitItem> PROCESSOR_UNIT =
            ITEMS.register("processor_unit", () -> new ProcessorUnitItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
