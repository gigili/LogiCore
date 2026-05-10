package dev.gacbl.logicore.blocks.datacable;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataCableModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final DeferredBlock<DataCableBlock> DATA_CABLE_BLOCK =
            BLOCKS.registerBlock("data_cable", DataCableBlock::new,
                    props -> props.mapColor(MapColor.COLOR_CYAN).strength(0.5f).noOcclusion()
                            .isViewBlocking((blockState, blockGetter, blockPos) -> false)
            );

    public static final DeferredHolder<Item, BlockItem> DATA_CABLE_ITEM =
            ITEMS.registerItem("data_cable", props -> new BlockItem(DATA_CABLE_BLOCK.get(), props));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<DataCableBlockEntity>> DATA_CABLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("data_cable", () -> new BlockEntityType<>(
                    DataCableBlockEntity::new, DATA_CABLE_BLOCK.get()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
