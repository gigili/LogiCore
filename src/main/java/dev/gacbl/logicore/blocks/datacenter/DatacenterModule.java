package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacenter.ui.DatacenterControllerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
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

import java.util.function.Supplier;

public class DatacenterModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredBlock<DatacenterControllerBlock> DATACENTER_CONTROLLER = BLOCKS.registerBlock(
            "datacenter_controller", DatacenterControllerBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(3.0F, 3.0F).requiresCorrectToolForDrops());

    public static final DeferredHolder<Item, BlockItem> DATACENTER_CONTROLLER_ITEM = ITEMS.registerItem("datacenter_controller", props -> new BlockItem(DATACENTER_CONTROLLER.get(), props));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DatacenterControllerBlockEntity>> DATACENTER_CONTROLLER_BE =
            BLOCK_ENTITIES.register("datacenter_controller",
                    () -> new BlockEntityType<>(DatacenterControllerBlockEntity::new, DATACENTER_CONTROLLER.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<DatacenterControllerMenu>> DATACENTER_CONTROLLER_MENU = MENUS.register("datacenter_controller_menu", () -> IMenuTypeExtension.create(DatacenterControllerMenu::new));

    public static final Supplier<SoundEvent> DATACENTER_AMBIENT = registerSoundEvent("datacenter_ambient");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        SOUND_EVENTS.register(eventBus);
        MENUS.register(eventBus);
        eventBus.addListener(DatacenterModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                DATACENTER_CONTROLLER_BE.get(),
                (be, context) -> be.getItemHandler()
        );
    }
}
