package dev.gacbl.logicore.blocks.computer;

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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ComputerModule {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LogiCore.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, ComputerBlock> COMPUTER_BLOCK =
            BLOCKS.register("computer", () -> new ComputerBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(0.5f).noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem> COMPUTER_ITEM =
            ITEMS.register("computer", () -> new BlockItem(COMPUTER_BLOCK.get(), new Item.Properties()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<ComputerBlockEntity>> COMPUTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("computer", () -> BlockEntityType.Builder.of(
                    ComputerBlockEntity::new, COMPUTER_BLOCK.get()).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<MenuType<?>, MenuType<ServerRackMenu>> SERVER_RACK_MENU =
            MENUS.register("computer_menu", () -> IMenuTypeExtension.create(ServerRackMenu::new));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        modEventBus.addListener(ComputerModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModCapabilities.CYCLE_PROVIDER,
                ComputerModule.COMPUTER_BLOCK_ENTITY.get(),
                (be, context) -> be.getCycleStorage()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ComputerModule.COMPUTER_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ComputerModule.COMPUTER_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );
    }
}
