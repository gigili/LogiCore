package dev.gacbl.logicore.blocks.cloud_interface;

import com.mojang.serialization.MapCodec;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudInterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<CloudInterfaceBlock> CODEC = simpleCodec(CloudInterfaceBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    protected CloudInterfaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static ShapedRecipeBuilder getRecipe(HolderGetter<Item> items) {
        return ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, CloudInterfaceModule.CLOUD_INTERFACE.get())
                .pattern("NRN")
                .pattern("PPP")
                .pattern("NRN")
                .define('N', ItemTags.create(Identifier.fromNamespaceAndPath("c", "ender_pearls")))
                .define('R', ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/redstone")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void destroy(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (level instanceof Level l) {
            BlockEntity be = l.getBlockEntity(pos);
            if (be instanceof CloudInterfaceBlockEntity cloudInterfaceBlockEntity) {
                if (cloudInterfaceBlockEntity.hasItem()) {
                    ItemStack stack = cloudInterfaceBlockEntity.extract();
                    Containers.dropItemStack(l, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
        }
        super.destroy(level, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, CloudInterfaceModule.CLOUD_INTERFACE_BE.get(), CloudInterfaceBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CloudInterfaceBlockEntity(blockPos, blockState);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos bePos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(bePos);
        if (!(be instanceof CloudInterfaceBlockEntity ci)) return InteractionResult.PASS;

        if (ci.hasItem()) {
            ItemStack stack = ci.extract();
            player.getInventory().placeItemBackInInventory(stack);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        InteractionResult result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (result != InteractionResult.PASS) {
            return result;
        }
        return this.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CloudInterfaceBlockEntity cloudBe) {
                cloudBe.setOwner(player.getUUID());
            }
        }
    }
}
