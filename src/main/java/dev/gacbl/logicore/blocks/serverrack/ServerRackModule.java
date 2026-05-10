package dev.gacbl.logicore.blocks.serverrack;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServerRackModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredBlock<ServerRackBlock> SERVER_RACK =
            BLOCKS.registerBlock("server_rack", ServerRackBlock::new,
                    props -> props.mapColor(MapColor.METAL).strength(3.0F, 3.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredHolder<Item, BlockItem> SERVER_RACK_ITEM =
            ITEMS.registerItem("server_rack", props -> new BlockItem(SERVER_RACK.get(), props));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerRackBlockEntity>> SERVER_RACK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("server_rack", () -> new BlockEntityType<>(
                    ServerRackBlockEntity::new, SERVER_RACK.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<ServerRackMenu>> SERVER_RACK_MENU =
            MENUS.register("server_rack_menu", () -> IMenuTypeExtension.create(ServerRackMenu::new));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        modEventBus.addListener(ServerRackModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );

        event.registerBlockEntity(
                ModCapabilities.CYCLE_PROVIDER,
                SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyHandler()
        );
    }
}
