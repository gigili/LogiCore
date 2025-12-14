package dev.gacbl.logicore.items.wrench;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WrenchModule {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);

    public static final DeferredHolder<Item, WrenchItem> WRENCH =
            ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
