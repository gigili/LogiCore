package dev.gacbl.logicore.blocks.research_station;

import dev.gacbl.logicore.LogiCore;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ResearchStationModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final BooleanProperty RESEARCHING = BooleanProperty.create("researching");

    public static final DeferredBlock<Block> RESEARCH_STATION = BLOCKS.register("research_station",
            () -> new ResearchStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> RESEARCH_STATION_ITEM = ITEMS.register("research_station",
            () -> new BlockItem(RESEARCH_STATION.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchStationBlockEntity>> RESEARCH_STATION_BE =
            BLOCK_ENTITIES.register("research_station",
                    () -> BlockEntityType.Builder.of(ResearchStationBlockEntity::new, RESEARCH_STATION.get()).build(null));

    /*public static final net.neoforged.neoforge.registries.DeferredHolder<MenuType<?>, MenuType<ResearchStationMenu>> GENERATOR_MENU =
            MENUS.register("research_station_menu", () -> IMenuTypeExtension.create(ResearchStationMenu::new));*/

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        eventBus.addListener(ResearchStationModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    }
}
