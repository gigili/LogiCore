package dev.gacbl.logicore.blocks.battery.basic;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BasicBatteryModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final DeferredHolder<Block, BasicBatteryBlock> BASIC_BATTERY =
            BLOCKS.register("basic_battery", () -> new BasicBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> SERVER_RACK_ITEM =
            ITEMS.register("basic_battery", () -> new BlockItem(BASIC_BATTERY.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicBatteryBlockEntity>> BASIC_BATTERY_BE =
            BLOCK_ENTITIES.register("basic_battery", () -> BlockEntityType.Builder.of(
                    BasicBatteryBlockEntity::new, BASIC_BATTERY.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(BasicBatteryModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                BASIC_BATTERY_BE.get(),
                (be, context) -> be.getEnergyStorage()
        );
    }
}
