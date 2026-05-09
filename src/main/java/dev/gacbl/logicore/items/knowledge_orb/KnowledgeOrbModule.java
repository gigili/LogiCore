package dev.gacbl.logicore.items.knowledge_orb;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class KnowledgeOrbModule {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);

    public static final DeferredHolder<Item, KnowledgeOrbItem> KNOWLEDGE_ORB =
            ITEMS.register("knowledge_orb", () -> new KnowledgeOrbItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
