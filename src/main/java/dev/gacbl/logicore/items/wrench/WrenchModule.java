package dev.gacbl.logicore.items.wrench;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WrenchModule {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(LogiCore.MOD_ID);

    public static final DeferredHolder<Item, WrenchItem> WRENCH = ITEMS.registerItem("wrench", WrenchItem::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
