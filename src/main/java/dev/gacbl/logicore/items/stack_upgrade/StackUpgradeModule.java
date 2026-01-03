package dev.gacbl.logicore.items.stack_upgrade;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class StackUpgradeModule {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);

    public static final DeferredHolder<Item, StackUpgradeItem> STACK_UPGRADE =
            ITEMS.register("stack_upgrade", () -> new StackUpgradeItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
