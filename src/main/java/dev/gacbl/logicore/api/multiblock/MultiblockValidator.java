package dev.gacbl.logicore.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class MultiblockValidator {

    public static class ValidationResult {
        public final BlockPos min;
        public final BlockPos max;
        public final int volume;

        public ValidationResult(BlockPos min, BlockPos max) {
            this.min = min;
            this.max = max;
            this.volume = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        }
    }

    public static ValidationResult detectRoom(Level level, BlockPos controllerPos, Direction facing,
                                              Predicate<BlockState> isFrame,
                                              Predicate<BlockState> isWall,
                                              Predicate<BlockState> isInterior,
                                              Predicate<BlockState> isController,
                                              int minSize, int maxSize) throws MultiblockValidationException {

        BlockPos start = controllerPos.relative(facing);

        Predicate<BlockState> anyOuterBlock = state -> isFrame.test(state) || isWall.test(state);

        if (anyOuterBlock.test(level.getBlockState(start))) {
            throw new MultiblockValidationException("errors.logicore.multiblock.multiple_controllers", start, level.getBlockState(start));
        }

        // We scan for limits. If we don't find a wall within maxSize, it's too big/open.
        int distDown = raycast(level, start, Direction.DOWN, anyOuterBlock, maxSize);
        int distUp = raycast(level, start, Direction.UP, anyOuterBlock, maxSize);
        int distNorth = raycast(level, start, Direction.NORTH, anyOuterBlock, maxSize);
        int distSouth = raycast(level, start, Direction.SOUTH, anyOuterBlock, maxSize);
        int distWest = raycast(level, start, Direction.WEST, anyOuterBlock, maxSize);
        int distEast = raycast(level, start, Direction.EAST, anyOuterBlock, maxSize);

        if (distDown == -1 || distUp == -1 || distNorth == -1 || distSouth == -1 || distWest == -1 || distEast == -1) {
            throw new MultiblockValidationException("errors.logicore.multiblock.too_large_or_open");
        }

        BlockPos minParams = start.offset(-distWest + 1, -distDown + 1, -distNorth + 1);
        BlockPos maxParams = start.offset(distEast - 1, distUp - 1, distSouth - 1);

        BlockPos wallMin = minParams.offset(-1, -1, -1);
        BlockPos wallMax = maxParams.offset(1, 1, 1);

        int width = wallMax.getX() - wallMin.getX() + 1;
        int height = wallMax.getY() - wallMin.getY() + 1;
        int depth = wallMax.getZ() - wallMin.getZ() + 1;

        if (width < minSize || height < minSize || depth < minSize) {
            throw new MultiblockValidationException("errors.logicore.multiblock.too_small");
        }
        if (width > maxSize || height > maxSize || depth > maxSize) {
            throw new MultiblockValidationException("errors.logicore.multiblock.too_large");
        }

        if (!isPositionOnFace(controllerPos, wallMin, wallMax)) {
            throw new MultiblockValidationException("errors.logicore.multiblock.controller_misplaced");
        }

        for (int x = wallMin.getX(); x <= wallMax.getX(); x++) {
            for (int y = wallMin.getY(); y <= wallMax.getY(); y++) {
                for (int z = wallMin.getZ(); z <= wallMax.getZ(); z++) {

                    BlockPos p = new BlockPos(x, y, z);

                    boolean onMinX = x == wallMin.getX();
                    boolean onMaxX = x == wallMax.getX();
                    boolean onMinY = y == wallMin.getY();
                    boolean onMaxY = y == wallMax.getY();
                    boolean onMinZ = z == wallMin.getZ();
                    boolean onMaxZ = z == wallMax.getZ();

                    int boundaryCount = (onMinX ? 1 : 0) + (onMaxX ? 1 : 0) +
                            (onMinY ? 1 : 0) + (onMaxY ? 1 : 0) +
                            (onMinZ ? 1 : 0) + (onMaxZ ? 1 : 0);

                    boolean isEdge = boundaryCount >= 2;
                    boolean isAnyBoundary = boundaryCount >= 1;

                    if (p.equals(controllerPos)) continue;

                    BlockState state = level.getBlockState(p);

                    if (isAnyBoundary) {
                        if (isController.test(state)) {
                            throw new MultiblockValidationException("errors.logicore.multiblock.duplicate_controller", p, state);
                        }

                        if (isEdge) {
                            if (!isFrame.test(state)) {
                                throw new MultiblockValidationException("errors.logicore.multiblock.invalid_frame", p, state);
                            }
                        } else {
                            if (!isWall.test(state)) {
                                throw new MultiblockValidationException("errors.logicore.multiblock.invalid_wall", p, state);
                            }
                        }
                    } else {
                        if (!isInterior.test(state)) {
                            throw new MultiblockValidationException("errors.logicore.multiblock.invalid_interior", p, state);
                        }
                    }
                }
            }
        }

        return new ValidationResult(wallMin, wallMax);
    }

    private static int raycast(Level level, BlockPos start, Direction dir, Predicate<BlockState> isWall, int limit) {
        for (int i = 1; i <= limit; i++) {
            BlockPos p = start.relative(dir, i);
            if (isWall.test(level.getBlockState(p))) return i;
        }
        return -1;
    }

    private static boolean isPositionOnFace(BlockPos pos, BlockPos min, BlockPos max) {
        boolean onX = pos.getX() == min.getX() || pos.getX() == max.getX();
        boolean onY = pos.getY() == min.getY() || pos.getY() == max.getY();
        boolean onZ = pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
        boolean inX = pos.getX() >= min.getX() && pos.getX() <= max.getX();
        boolean inY = pos.getY() >= min.getY() && pos.getY() <= max.getY();
        boolean inZ = pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        return (onX && inY && inZ) || (inX && onY && inZ) || (inX && inY && onZ);
    }
}
