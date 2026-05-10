package dev.gacbl.logicore.blocks.battery;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class BatteryModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final DeferredBlock<BatteryBlock> BATTERY_SMALL = registerBattery("small_battery", BatteryTier.SMALL);
    public static final DeferredBlock<BatteryBlock> BATTERY_MEDIUM = registerBattery("medium_battery", BatteryTier.MEDIUM);
    public static final DeferredBlock<BatteryBlock> BATTERY_LARGE = registerBattery("large_battery", BatteryTier.LARGE);
    public static final DeferredBlock<BatteryBlock> BATTERY_CREATIVE = registerBattery("creative_battery", BatteryTier.CREATIVE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseBatteryEntity>> BATTERY_BE = BLOCK_ENTITIES.register("battery",
            () -> new BlockEntityType<>(BaseBatteryEntity::new,
                    BATTERY_SMALL.get(),
                    BATTERY_MEDIUM.get(),
                    BATTERY_LARGE.get(),
                    BATTERY_CREATIVE.get()
            ));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(BatteryModule::registerCapabilities);
    }

    private static DeferredBlock<BatteryBlock> registerBattery(String name, BatteryTier tier) {
        DeferredBlock<BatteryBlock> block = BLOCKS.registerBlock(name,
                props -> new BatteryBlock(props, tier),
                props -> props.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops()
        );
        ITEMS.registerItem(name, props -> new BlockItem(block.get(), props));
        return block;
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                BATTERY_BE.get(),
                (be, context) -> be.getEnergyHandler()
        );
    }
}
