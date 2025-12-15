package dev.gacbl.logicore.blocks.battery.basic;

import dev.gacbl.logicore.blocks.battery.BaseBatteryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBatteryBlockEntity extends BaseBatteryEntity {
    public BasicBatteryBlockEntity(BlockPos pos, BlockState blockState) {
        super(BasicBatteryModule.BASIC_BATTERY_BE.get(), pos, blockState, 100_000, 10_000, 10_000);
    }


    public void dropContents() {

    }
}
