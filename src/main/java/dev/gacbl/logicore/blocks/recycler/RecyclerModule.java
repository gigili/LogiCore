package dev.gacbl.logicore.blocks.recycler;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.items.recycler.RecyclerBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecyclerModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final BooleanProperty CRUSHING = BooleanProperty.create("crushing");

    public static final DeferredBlock<Block> RECYCLER = BLOCKS.register("recycler",
            () -> new RecyclerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            ));

    public static final DeferredHolder<Item, BlockItem> RECYCLER_ITEM = ITEMS.register("recycler",
            () -> new RecyclerBlockItem(RECYCLER.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerBlockEntity>> RECYCLER_BE =
            BLOCK_ENTITIES.register("recycler",
                    () -> BlockEntityType.Builder.of(RecyclerBlockEntity::new, RECYCLER.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
