package dev.gacbl.logicore.blocks.repair_station;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.repair_station.ui.RepairStationMenu;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RepairStationModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final BooleanProperty REPAIRING = BooleanProperty.create("repairing");

    public static final DeferredBlock<Block> REPAIR_STATION = BLOCKS.register("repair_station",
            () -> new RepairStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> REPAIR_STATION_ITEM = ITEMS.register("repair_station",
            () -> new BlockItem(REPAIR_STATION.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepairStationBlockEntity>> REPAIR_STATION_BE =
            BLOCK_ENTITIES.register("repair_station",
                    () -> BlockEntityType.Builder.of(RepairStationBlockEntity::new, REPAIR_STATION.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<RepairStationMenu>> REPAIR_STATION_MENU =
            MENUS.register("repair_station_menu", () -> IMenuTypeExtension.create(RepairStationMenu::new));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        eventBus.addListener(RepairStationModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModCapabilities.CYCLE_CONSUMER,
                REPAIR_STATION_BE.get(),
                (be, context) -> be
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                REPAIR_STATION_BE.get(),
                (be, context) -> be.getItemHandler()
        );
    }
}
