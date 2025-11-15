package dev.gacbl.logicore.blocks.serverrack;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ServerRackModule {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, LogiCore.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, ServerRackBlock> SERVER_RACK_BLOCK =
            BLOCKS.register("server_rack", () -> new ServerRackBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem> SERVER_RACK_ITEM =
            ITEMS.register("server_rack", () -> new BlockItem(SERVER_RACK_BLOCK.get(), new Item.Properties()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerRackBlockEntity>> SERVER_RACK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("server_rack", () -> BlockEntityType.Builder.of(
                    ServerRackBlockEntity::new, SERVER_RACK_BLOCK.get()).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<MenuType<?>, MenuType<ServerRackMenu>> SERVER_RACK_MENU =
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
                Capabilities.ItemHandler.BLOCK,
                ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );

        // Attach Cycle Storage capability to Server Rack
        event.registerBlockEntity(
                ModCapabilities.CYCLE_STORAGE,
                ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getCycleStorage()
        );

        // Attach Energy capability to Server Rack
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyStorage()
        );
    }
}
