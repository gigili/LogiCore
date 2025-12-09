package dev.gacbl.logicore.entity.drone.goals;

import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlockEntity;
import dev.gacbl.logicore.core.ModCapabilities;
import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;

public class RechargeIfEmptyGoal extends Goal {
    private final DroneEntity drone;

    public RechargeIfEmptyGoal(DroneEntity drone) {
        this.drone = drone;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return drone.getHomePos() != null && drone.getCyclesStored() < (drone.getCycleStorage().getCycleCapacity() * 0.1);
    }

    @Override
    public boolean canContinueToUse() {
        return drone.getHomePos() != null && drone.getCyclesStored() < drone.getCycleStorage().getCycleCapacity();
    }

    @Override
    public void start() {
    }

    @Override
    public void tick() {
        BlockPos home = drone.getHomePos();
        if (home == null) return;

        double distSq = drone.distanceToSqr(home.getX() + 0.5, home.getY() + 1.7, home.getZ() + 0.5);

        if (distSq > 2.0) {
            drone.getNavigation().moveTo(home.getX() + 0.5, home.getY() + 1.7, home.getZ() + 0.5, 1.0D);
        } else {
            drone.getNavigation().stop();
            drone.getLookControl().setLookAt(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);

            BlockEntity be = drone.level().getBlockEntity(home);
            if (be instanceof DroneBayBlockEntity bay) {
                bay.setDockedName(drone.getDisplayName().getString());

                var cap = drone.level().getCapability(ModCapabilities.CYCLE_CONSUMER, home, null);
                if (cap != null) {
                    long maxTransfer = 100;
                    long needed = drone.getCycleStorage().getCycleCapacity() - drone.getCyclesStored();
                    long toExtract = Math.min(maxTransfer, needed);
                    long extracted = cap.extractCycles(toExtract, false);
                    drone.receiveCycles(extracted, false);
                }
            }
        }
    }

    @Override
    public void stop() {
        BlockPos home = drone.getHomePos();
        if (home != null) {
            BlockEntity be = drone.level().getBlockEntity(home);
            if (be instanceof DroneBayBlockEntity bay) {
                bay.clearDockedName();
            }
        }
    }
}
