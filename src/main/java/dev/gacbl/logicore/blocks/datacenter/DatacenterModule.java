package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DatacenterModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, LogiCore.MOD_ID);

    public static final DeferredBlock<Block> DATACENTER_CONTROLLER = BLOCKS.register("datacenter_controller",
            () -> new DatacenterControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Item, BlockItem> DATACENTER_CONTROLLER_ITEM = ITEMS.register("datacenter_controller",
            () -> new BlockItem(DATACENTER_CONTROLLER.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DatacenterControllerBlockEntity>> DATACENTER_CONTROLLER_BE =
            BLOCK_ENTITIES.register("datacenter_controller",
                    () -> BlockEntityType.Builder.of(DatacenterControllerBlockEntity::new, DATACENTER_CONTROLLER.get()).build(null));

    public static final Supplier<SoundEvent> DATACENTER_AMBIENT = registerSoundEvent("datacenter_ambient");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        SOUND_EVENTS.register(eventBus);
    }
}
