package dev.gacbl.logicore.blocks.cloud_interface;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CloudInterfaceModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final DeferredHolder<Block, CloudInterfaceBlock> CLOUD_INTERFACE =
            BLOCKS.register("cloud_interface", () -> new CloudInterfaceBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(0.5f).noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> COMPUTER_ITEM =
            ITEMS.register("cloud_interface", () -> new BlockItem(CLOUD_INTERFACE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CloudInterfaceBlockEntity>> CLOUD_INTERFACE_BE =
            BLOCK_ENTITIES.register("cloud_interface", () -> BlockEntityType.Builder.of(
                    CloudInterfaceBlockEntity::new, CLOUD_INTERFACE.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(CloudInterfaceModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModCapabilities.CYCLE_PROVIDER,
                CLOUD_INTERFACE_BE.get(),
                (be, context) -> be
        );
    }
}
