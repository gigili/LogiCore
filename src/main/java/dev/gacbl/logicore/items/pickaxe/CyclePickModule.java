package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CyclePickModule {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(LogiCore.MOD_ID);

    public static final DeferredHolder<Item, CyclePickItem> CYCLE_PICK = ITEMS.registerItem("cycle_pick", CyclePickItem::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
