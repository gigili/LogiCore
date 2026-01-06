package dev.gacbl.logicore.blocks.research_station;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ResearchStationBlockEntity extends BlockEntity {
    public ResearchStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ResearchStationModule.RESEARCH_STATION_BE.get(), pos, blockState);
    }
}
