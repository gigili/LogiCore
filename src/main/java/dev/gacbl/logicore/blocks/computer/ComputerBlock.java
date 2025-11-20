package dev.gacbl.logicore.blocks.computer;

import com.mojang.serialization.MapCodec;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
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

public class ComputerBlock extends BaseEntityBlock {
    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        // 1. BOTTOM PART: 16x7x16 (Width x Height x Depth)
        VoxelShape bottom = Block.box(0, 0, 0, 16, 7, 16);

        // 2. TOP PARTS: 16x9x9 (Width x Height x Depth)

        // NORTH (Default): Face points -Z. Screen is at Back (+Z).
        // Box: X(0-16), Y(7-16), Z(7-16) -> Depth is 16-7 = 9.
        VoxelShape topNorth = Block.box(0, 7, 7, 16, 16, 16);

        // SOUTH: Face points +Z. Screen is at Back (-Z).
        // Box: X(0-16), Y(7-16), Z(0-9).
        VoxelShape topSouth = Block.box(0, 7, 0, 16, 16, 9);

        // EAST: Face points +X. Screen is at Back (-X).
        // Box: X(0-9), Y(7-16), Z(0-16).
        VoxelShape topEast = Block.box(0, 7, 0, 9, 16, 16);

        // WEST: Face points -X. Screen is at Back (+X).
        // Box: X(7-16), Y(7-16), Z(0-16).
        VoxelShape topWest = Block.box(7, 7, 0, 16, 16, 16);

        // 3. COMBINE SHAPES
        SHAPES[2] = Shapes.or(bottom, topNorth); // North
        SHAPES[3] = Shapes.or(bottom, topSouth); // South
        SHAPES[4] = Shapes.or(bottom, topWest);  // West
        SHAPES[5] = Shapes.or(bottom, topEast);  // East

        // Fallback for UP/DOWN
        SHAPES[0] = SHAPES[2];
        SHAPES[1] = SHAPES[2];
    }

    public ComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ComputerModule.COMPUTER_BLOCK.get())
                .pattern("RNR")
                .pattern("NPN")
                .pattern("RNR")
                .define('N', Items.NETHER_STAR)
                .define('R', Items.REDSTONE)
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
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ComputerBlockEntity computerBlockEntity) {
            computerBlockEntity.dropContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, ComputerModule.COMPUTER_BLOCK_ENTITY.get(), ComputerBlockEntity::serverTick);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos bePos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(bePos);

            if (be instanceof ComputerBlockEntity serverRack) {
                player.openMenu(serverRack, bePos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
