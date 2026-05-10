package dev.gacbl.logicore.blocks.cloud_interface;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CloudInterfaceModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);

    public static final DeferredBlock<CloudInterfaceBlock> CLOUD_INTERFACE =
            BLOCKS.registerBlock("cloud_interface", CloudInterfaceBlock::new,
                    props -> props.requiresCorrectToolForDrops().strength(0.5f).noOcclusion());

    public static final DeferredHolder<Item, BlockItem> CLOUD_INTERFACE_ITEM =
            ITEMS.registerItem("cloud_interface", props -> new BlockItem(CLOUD_INTERFACE.get(), props));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CloudInterfaceBlockEntity>> CLOUD_INTERFACE_BE =
            BLOCK_ENTITIES.register("cloud_interface", () -> new BlockEntityType<>(
                    CloudInterfaceBlockEntity::new, CLOUD_INTERFACE.get()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        //modEventBus.addListener(CloudInterfaceModule::registerCapabilities);
    }

    //private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    //    event.registerBlockEntity(
    //            ModCapabilities.CYCLE_PROVIDER,
    //            CLOUD_INTERFACE_BE.get(),
    //            CloudInterfaceBlockEntity::getCycleCapability
    //    );
    //
    //    // AE2 integration not yet available for NeoForge 26.1
    //    //if (ModList.get().isLoaded("ae2")) {
    //    //    dev.gacbl.logicore.api.compat.ae2.Ae2Helper.registerCapabilities(event);
    //    //}
    //}
}
