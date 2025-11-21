package dev.gacbl.logicore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockValidationException extends Exception {
    public String message;
    public BlockPos pos;
    public BlockState state;

    MultiblockValidationException() {
        this("errors.logicore.multiblock.general_error", null, null);
    }

    MultiblockValidationException(String message) {
        this(message, null, null);
    }

    MultiblockValidationException(BlockPos pos) {
        this("errors.logicore.multiblock.general_error", pos, null);
    }

    MultiblockValidationException(BlockState state) {
        this("errors.logicore.multiblock.general_error", null, state);
    }

    MultiblockValidationException(String message, BlockPos pos, BlockState state) {
        this.message = message;
        this.pos = pos;
        this.state = state;
    }
}
