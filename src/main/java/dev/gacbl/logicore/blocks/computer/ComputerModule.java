package dev.gacbl.logicore.blocks.computer;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ComputerModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredBlock<ComputerBlock> COMPUTER_BLOCK =
            BLOCKS.registerBlock("computer", ComputerBlock::new,
                    props -> props.requiresCorrectToolForDrops().strength(0.5f).noOcclusion());

    public static final DeferredHolder<Item, BlockItem> COMPUTER_ITEM =
            ITEMS.registerItem("computer", props -> new BlockItem(COMPUTER_BLOCK.get(), props));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ComputerBlockEntity>> COMPUTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("computer", () -> new BlockEntityType<>(
                    ComputerBlockEntity::new, COMPUTER_BLOCK.get()));

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
                COMPUTER_BLOCK_ENTITY.get(),
                (be, context) -> be
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                COMPUTER_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyHandler()
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                COMPUTER_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );
    }
}
