package dev.gacbl.logicore.blocks.battery;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BatteryBlock extends BaseEntityBlock {
    private final BatteryTier tier;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<BatteryBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            BatteryTier.CODEC.fieldOf("tier").forGetter(BatteryBlock::getTier)
    ).apply(instance, BatteryBlock::new));

    public BatteryBlock(Properties properties, BatteryTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BatteryTier getTier() {
        return tier;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BaseBatteryEntity(pos, state);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, BatteryModule.BATTERY_BE.get(), BaseBatteryEntity::serverTick);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        int capacity = 0;
        int transferRate = 0;

        switch (tier) {
            case SMALL -> {
                capacity = Config.SMALL_BATTERY_CAPACITY.get();
                transferRate = Config.SMALL_BATTERY_TRANSFER_RATE.get();
            }
            case MEDIUM -> {
                capacity = Config.MEDIUM_BATTERY_CAPACITY.get();
                transferRate = Config.MEDIUM_BATTERY_TRANSFER_RATE.get();
            }
            case LARGE -> {
                capacity = Config.LARGE_BATTERY_CAPACITY.get();
                transferRate = Config.LARGE_BATTERY_TRANSFER_RATE.get();
            }
        }

        tooltipComponents.add(Component.translatable("tooltip.logicore.battery_capacity", Utils.formatValues(capacity)));
        tooltipComponents.add(Component.translatable("tooltip.logicore.battery_transfer_rate", Utils.formatValues(transferRate)));
    }
}
