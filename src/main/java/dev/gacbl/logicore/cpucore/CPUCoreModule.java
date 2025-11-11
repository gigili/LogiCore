package dev.gacbl.logicore.cpucore;

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

public class CPUCoreModule {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, LogiCore.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, CPUCoreBlock> CPU_CORE_BLOCK =
            BLOCKS.register("cpu_core", () -> new CPUCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.0f).requiresCorrectToolForDrops()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem> CPU_CORE_ITEM =
            ITEMS.register("cpu_core", () -> new BlockItem(CPU_CORE_BLOCK.get(), new Item.Properties()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<CPUCoreBlockEntity>> CPU_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cpu_core", () -> BlockEntityType.Builder.of(
                    CPUCoreBlockEntity::new, CPU_CORE_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
