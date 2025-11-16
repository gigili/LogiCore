package dev.gacbl.logicore.blocks.datacable;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataCableModule {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, LogiCore.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, LogiCore.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, DataCableBlock> DATA_CABLE_BLOCK =
            BLOCKS.register("data_cable", () -> new DataCableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN).strength(0.5f).noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem> DATA_CABLE_ITEM =
            ITEMS.register("data_cable", () -> new BlockItem(DATA_CABLE_BLOCK.get(), new Item.Properties()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<DataCableBlockEntity>> DATA_CABLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("data_cable", () -> BlockEntityType.Builder.of(
                    DataCableBlockEntity::new, DATA_CABLE_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
