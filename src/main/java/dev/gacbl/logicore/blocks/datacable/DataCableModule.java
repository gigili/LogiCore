package dev.gacbl.logicore.blocks.datacable;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataCableModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, DataCableBlock> DATA_CABLE_BLOCK =
            BLOCKS.register("data_cable", () -> new DataCableBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_CYAN)
                                    .strength(0.5f)
                                    .noOcclusion()
                                    .isViewBlocking((blockState, blockGetter, blockPos) -> false)
                                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "data_cable")))
                    )
            );

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem> DATA_CABLE_ITEM = ITEMS.registerSimpleBlockItem(DATA_CABLE_BLOCK, new Item.Properties().useBlockDescriptionPrefix());

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<DataCableBlockEntity>> DATA_CABLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("data_cable", () -> new BlockEntityType<>(
                    DataCableBlockEntity::new, DATA_CABLE_BLOCK.get()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
