package dev.gacbl.logicore.blocks.computer;

import com.mojang.serialization.MapCodec;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComputerBlock extends BaseEntityBlock {
    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        VoxelShape bottom = Block.box(0, 0, 0, 16, 7, 16);
        VoxelShape topNorth = Block.box(0, 7, 7, 16, 16, 16);
        VoxelShape topSouth = Block.box(0, 7, 0, 16, 16, 9);
        VoxelShape topEast = Block.box(0, 7, 0, 9, 16, 16);
        VoxelShape topWest = Block.box(7, 7, 0, 16, 16, 16);

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
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static ShapedRecipeBuilder getRecipe(HolderGetter<Item> items) {
        return ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, ComputerModule.COMPUTER_BLOCK.get())
                .pattern("RNR")
                .pattern("EPE")
                .pattern("RNR")
                .define('N', ItemTags.create(Identifier.fromNamespaceAndPath("c", "gems/quartz")))
                .define('R', ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/redstone")))
                .define('E', ItemTags.create(Identifier.fromNamespaceAndPath("c", "ender_pearls")))
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
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
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


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, ComputerModule.COMPUTER_BLOCK_ENTITY.get(), dev.gacbl.logicore.core.CoreCycleProviderBlockEntity::serverTick);
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
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(bePos);

            if (be instanceof ComputerBlockEntity serverRack) {
                player.openMenu(serverRack, bePos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide() && stack.getItem() instanceof ProcessorUnitItem) {
            if (level.getBlockEntity(pos) instanceof ComputerBlockEntity pc) {
                var handler = pc.getItemHandler();
                var slots = handler.copyToList();
                boolean inserted = false;
                for (int slot = 0; slot < slots.size(); slot++) {
                    ItemStack s = slots.get(slot);
                    if (s.isEmpty() && !stack.isEmpty()) {
                        handler.set(slot, ItemResource.of(stack.getItem()), 1);
                        stack.shrink(1);
                        inserted = true;
                        break;
                    }
                }

                if (inserted) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    return InteractionResult.CONSUME;
                }
            }
        }

        InteractionResult result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (result != InteractionResult.PASS) {
            return result;
        }
        return this.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public boolean onDestroyedByPlayer(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull ItemStack tool, boolean willHarvest, @NotNull FluidState fluidState) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity computer) {
                computer.dropContents();
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluidState);
    }
}
