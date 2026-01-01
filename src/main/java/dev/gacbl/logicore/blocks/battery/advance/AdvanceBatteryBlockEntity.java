package dev.gacbl.logicore.blocks.battery.advance;

import dev.gacbl.logicore.blocks.battery.BaseBatteryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvanceBatteryBlockEntity extends BaseBatteryEntity {
    public AdvanceBatteryBlockEntity(BlockPos pos, BlockState blockState) {
        super(AdvanceBatteryModule.ADVANCE_BATTERY_BE.get(), pos, blockState, 500_000, 50_000, 50_000);
    }

    public void dropContents() {

    }
}
