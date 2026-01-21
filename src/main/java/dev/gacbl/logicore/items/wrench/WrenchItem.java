package dev.gacbl.logicore.items.wrench;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, WrenchModule.WRENCH.get())
                .pattern(" QP")
                .pattern(" SI")
                .pattern("S  ")
                .define('S', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden")))
                .define('I', Items.IRON_INGOT)
                .define('Q', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/quartz")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (!blockKey.getNamespace().equals(LogiCore.MOD_ID)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            dismantleBlock(level, pos, state, player);
            return InteractionResult.SUCCESS;
        }

        return rotateBlock(level, pos, state);
    }

    private InteractionResult rotateBlock(Level level, BlockPos pos, BlockState state) {
        BlockState rotatedState = state.rotate(level, pos, Rotation.CLOCKWISE_90);

        if (rotatedState != state) {
            level.setBlockAndUpdate(pos, rotatedState);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void dismantleBlock(Level level, BlockPos pos, BlockState state, Player player) {
        ItemStack itemStack = new ItemStack(state.getBlock());

        if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
        }

        level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);

        level.destroyBlock(pos, false);
    }
}
