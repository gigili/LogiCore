package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CyclePickModule {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);

    public static final DeferredHolder<Item, CyclePickItem> CYCLE_PICK =
            ITEMS.register("cycle_pick", () -> new CyclePickItem(ModTiers.CYCLE_PICK, (new Item.Properties()).fireResistant().attributes(PickaxeItem.createAttributes(ModTiers.CYCLE_PICK, 1.0F, -2.8F))));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
