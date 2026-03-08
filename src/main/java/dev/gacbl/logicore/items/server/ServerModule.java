package dev.gacbl.logicore.items.server;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.items.server.ui.ServerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServerModule {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredHolder<Item, ServerItem> SERVER = ITEMS.register("server", ServerItem::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ServerMenu>> SERVER_MENU = MENUS.register("server_menu", () -> IMenuTypeExtension.create((windowId, inv, data) -> new ServerMenu(windowId, inv, inv.player.getMainHandItem())));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
    }
}
