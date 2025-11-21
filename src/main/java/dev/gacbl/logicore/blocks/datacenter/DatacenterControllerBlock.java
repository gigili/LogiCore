package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatacenterControllerBlock extends Block implements EntityBlock {
    // UPDATED: Use generic FACING (6 directions) instead of HORIZONTAL_FACING
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public DatacenterControllerBlock(Properties properties) {
        super(properties);
        // Default state facing NORTH, but supports all directions now
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FORMED, false));
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, DataCableModule.DATA_CABLE_ITEM.get())
                .pattern("III")
                .pattern("OPO")
                .pattern("III")
                .define('I', Items.IRON_BLOCK)
                .define('O', Items.OBSIDIAN)
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // UPDATED: Allow placement on ceilings/floors by looking at the nearest looking direction
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    // Added rotation support for 6-way facing
    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DatacenterControllerBlockEntity controller) {
                // Toggle formation or open GUI
                if (!state.getValue(FORMED)) {
                    controller.attemptFormation();
                    if (level.getServer() != null) {
                        level.getServer().executeIfPossible(() -> {
                            if (state.getValue(FORMED)) {
                                Component msg = Component.translatable("message.logicore.datacenter.formed");
                                player.displayClientMessage(msg, true);
                            } else {
                                String errorPos = controller.lastException != null && controller.lastException.pos != null ? controller.lastException.pos.toShortString() : "";
                                Component msg = controller.lastException != null ? Component.translatable(controller.lastException.message, errorPos) : Component.translatable("errors.logicore.datacenter.invalid_form");
                                player.displayClientMessage(msg, true);
                            }
                        });
                    }
                } else {
                    // Open GUI logic here later
                    player.displayClientMessage(Component.translatable("message.logicore.datacenter.formed"), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DatacenterControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof DatacenterControllerBlockEntity controller) {
                controller.tick(lvl, pos, st);
            }
        };
    }
}
