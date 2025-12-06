package dev.gacbl.logicore.blocks.drone_bay;

import com.mojang.serialization.MapCodec;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroneBayBlock extends BaseEntityBlock {
    public static final MapCodec<DroneBayBlock> CODEC = simpleCodec(DroneBayBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        VoxelShape baseNorth = Block.box(1, 0, 0, 15, 2, 14);
        VoxelShape wallNorth = Block.box(1, 0, 14, 15, 16, 16);
        VoxelShape shapeNorth = Shapes.or(baseNorth, wallNorth);

        VoxelShape baseSouth = Block.box(1, 0, 2, 15, 2, 16);
        VoxelShape wallSouth = Block.box(1, 0, 0, 15, 16, 2);
        VoxelShape shapeSouth = Shapes.or(baseSouth, wallSouth);

        VoxelShape baseWest = Block.box(0, 0, 1, 14, 2, 15);
        VoxelShape wallWest = Block.box(14, 0, 1, 16, 16, 15);
        VoxelShape shapeWest = Shapes.or(baseWest, wallWest);

        VoxelShape baseEast = Block.box(2, 0, 1, 16, 2, 15);
        VoxelShape wallEast = Block.box(0, 0, 1, 2, 16, 15);
        VoxelShape shapeEast = Shapes.or(baseEast, wallEast);

        SHAPES[2] = shapeNorth;
        SHAPES[3] = shapeSouth;
        SHAPES[4] = shapeWest;
        SHAPES[5] = shapeEast;

        // Fallback for UP/DOWN (Default to North)
        SHAPES[0] = shapeNorth;
        SHAPES[1] = shapeNorth;
    }

    protected DroneBayBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, DroneBayModule.DRONE_BAY.get())
                .pattern("IGI")
                .pattern("RPR")
                .pattern("IGI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('G', Items.GOLD_INGOT)
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new DroneBayBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.FAIL;
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof DroneBayBlockEntity controller) {
                controller.tick(lvl, pos, st);
            }
        };
    }
}
