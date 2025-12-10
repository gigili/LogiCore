package dev.gacbl.logicore.blocks.serverrack;

import com.mojang.serialization.MapCodec;
import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerRackBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final MapCodec<ServerRackBlock> CODEC = simpleCodec(ServerRackBlock::new);

    public ServerRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(ServerRackModule.GENERATING, false));
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ServerRackModule.SERVER_RACK_BLOCK.get())
                .pattern("RNR")
                .pattern("EPE")
                .pattern("RNR")
                .define('N', Items.QUARTZ)
                .define('R', Items.REDSTONE)
                .define('E', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ender_pearls")))
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, ServerRackModule.GENERATING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(ServerRackModule.GENERATING, false);
        }
        return null;
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
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
        return SHAPE;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(state, level, pos))) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                BlockPos blockpos = pos.below();
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockstate));
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ServerRackBlockEntity(pos, state) : null;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos bePos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockEntity be = level.getBlockEntity(bePos);

            if (be instanceof ServerRackBlockEntity serverRack) {
                player.openMenu(serverRack, bePos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ServerRackBlockEntity serverRack) {
                    serverRack.dropContents();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return true;
        } else {
            BlockPos posBelow = pos.below();
            BlockState stateBelow = level.getBlockState(posBelow);
            return stateBelow.is(this) && stateBelow.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (facing.getAxis() == Direction.Axis.Y) {
            if ((half == DoubleBlockHalf.LOWER && facing == Direction.UP) || (half == DoubleBlockHalf.UPPER && facing == Direction.DOWN)) {
                if (!facingState.is(this) || facingState.getValue(HALF) == half) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        if (half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(), ServerRackBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide) return;

        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.above(), this);
        level.updateNeighborsAt(pos.below(), this);
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.getValue(ServerRackModule.GENERATING) || !Config.SERVER_RACK_PRODUCES_PARTICLES.get()) {
            return;
        }

        if (random.nextInt(5) == 0) {
            Direction facing = state.getValue(FACING);
            Direction left = facing.getCounterClockWise();
            Direction right = facing.getClockWise();

            BlockPos leftPos = pos.relative(left);
            if (level.getBlockState(leftPos).isAir()) {
                spawnSmokeAt(level, pos, left, random);
            }

            BlockPos rightPos = pos.relative(right);
            if (level.getBlockState(rightPos).isAir()) {
                spawnSmokeAt(level, pos, right, random);
            }
        }
    }

    private void spawnSmokeAt(Level level, BlockPos pos, Direction direction, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;

        x += direction.getStepX() * 0.6D;
        z += direction.getStepZ() * 0.6D;

        y += (random.nextDouble() * 0.8D) + 0.1D;

        level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
