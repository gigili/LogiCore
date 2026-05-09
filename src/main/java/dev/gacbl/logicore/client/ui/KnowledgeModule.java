package dev.gacbl.logicore.client.ui;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class KnowledgeModule {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<KnowledgeMenu>> KNOWLEDGE_MENU = MENUS.register("knowledge", () -> IMenuTypeExtension.create((windowId, inv, data) -> new KnowledgeMenu(windowId, inv)));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
