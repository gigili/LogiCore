package dev.gacbl.logicore.blocks.recycler;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.recycler.ui.RecyclerMenu;
import dev.gacbl.logicore.core.ModCapabilities;
import dev.gacbl.logicore.items.recycler.RecyclerBlockItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecyclerModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final BooleanProperty CRUSHING = BooleanProperty.create("crushing");

    public static final DeferredBlock<RecyclerBlock> RECYCLER = BLOCKS.registerBlock("recycler", RecyclerBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(3.0F, 3.0F).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredHolder<Item, BlockItem> RECYCLER_ITEM = ITEMS.registerItem("recycler",
            props -> new RecyclerBlockItem(RECYCLER.get(), props));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerBlockEntity>> RECYCLER_BE =
            BLOCK_ENTITIES.register("recycler",
                    () -> new BlockEntityType<>(RecyclerBlockEntity::new, RECYCLER.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<RecyclerMenu>> RECYCLER_MENU =
            MENUS.register("recycler_menu", () -> IMenuTypeExtension.create(RecyclerMenu::new));


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        eventBus.addListener(RecyclerModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                RECYCLER_BE.get(),
                (be, context) -> be.getItemHandler()
        );

        event.registerBlockEntity(
                ModCapabilities.CYCLE_PROVIDER,
                RECYCLER_BE.get(),
                (be, context) -> be
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                RECYCLER_BE.get(),
                (be, context) -> be.getEnergyHandler()
        );
    }
}
