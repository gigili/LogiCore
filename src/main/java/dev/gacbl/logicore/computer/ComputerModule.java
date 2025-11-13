package dev.gacbl.logicore.computer;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ComputerModule {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LogiCore.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, ComputerBlock> COMPUTER_BLOCK =
            BLOCKS.register("computer", () -> new ComputerBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(0.5f).noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem> COMPUTER_ITEM =
            ITEMS.register("computer", () -> new BlockItem(COMPUTER_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
