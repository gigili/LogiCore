package dev.gacbl.logicore.blocks.datacenter_port;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DatacenterPortModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final DeferredBlock<Block> DATACENTER_PORT = BLOCKS.register("datacenter_port",
            () -> new DatacenterPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Item, BlockItem> DATACENTER_PORT_ITEM = ITEMS.register("datacenter_port",
            () -> new BlockItem(DATACENTER_PORT.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DatacenterPortBlockEntity>> DATACENTER_PORT_BE =
            BLOCK_ENTITIES.register("datacenter_port",
                    () -> BlockEntityType.Builder.of(DatacenterPortBlockEntity::new, DATACENTER_PORT.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        eventBus.addListener(DatacenterPortModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModCapabilities.CYCLE_PROVIDER,
                DatacenterPortModule.DATACENTER_PORT_BE.get(),
                (blockEntity, context) -> blockEntity
        );
    }
}
