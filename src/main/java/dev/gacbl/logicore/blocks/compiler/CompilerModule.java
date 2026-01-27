package dev.gacbl.logicore.blocks.compiler;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerMenu;
import dev.gacbl.logicore.core.ModCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CompilerModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LogiCore.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogiCore.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogiCore.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LogiCore.MOD_ID);

    public static final DeferredHolder<Block, CompilerBlock> COMPILER_BLOCK =
            BLOCKS.register("compiler", () -> new CompilerBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(0.5f).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "compiler")))));

    public static final DeferredHolder<Item, BlockItem> COMPILER_ITEM = ITEMS.registerSimpleBlockItem(COMPILER_BLOCK, new Item.Properties().useBlockDescriptionPrefix());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompilerBlockEntity>> COMPILER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("compiler", () -> new BlockEntityType<>(
                    CompilerBlockEntity::new, COMPILER_BLOCK.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<CompilerMenu>> COMPILER_MENU =
            MENUS.register("compiler_menu", () -> IMenuTypeExtension.create(CompilerMenu::new));

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LogiCore.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, LogiCore.MOD_ID);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        SERIALIZERS.register(modEventBus);
        RECIPE_TYPE.register(modEventBus);
        modEventBus.addListener(CompilerModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModCapabilities.CYCLE_CONSUMER,
                CompilerModule.COMPILER_BLOCK_ENTITY.get(),
                (be, context) -> be.getCycleStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CompilerModule.COMPILER_BLOCK_ENTITY.get(),
                CompilerBlockEntity::getItemHandler
        );
    }
}
