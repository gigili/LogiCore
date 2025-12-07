package dev.gacbl.logicore.blocks.generator;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.generator.ui.GeneratorMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GeneratorModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredBlock<Block> GENERATOR = BLOCKS.register("generator",
            () -> new GeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> GENERATOR_ITEM = ITEMS.register("generator",
            () -> new BlockItem(GENERATOR.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR_BE =
            BLOCK_ENTITIES.register("generator",
                    () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR.get()).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<MenuType<?>, MenuType<GeneratorMenu>> GENERATOR_MENU =
            MENUS.register("generator_menu", () -> IMenuTypeExtension.create(GeneratorMenu::new));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        eventBus.addListener(GeneratorModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                GeneratorModule.GENERATOR_BE.get(),
                GeneratorBlockEntity::getItemHandler
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                GeneratorModule.GENERATOR_BE.get(),
                (be, context) -> be.getEnergyStorage()
        );
    }
}
