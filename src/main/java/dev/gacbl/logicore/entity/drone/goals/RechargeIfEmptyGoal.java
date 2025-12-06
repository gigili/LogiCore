package dev.gacbl.logicore.entity.drone.goals;

import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RechargeIfEmptyGoal extends Goal {
    private final DroneEntity drone;

    public RechargeIfEmptyGoal(DroneEntity drone) {
        this.drone = drone;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Go home if energy is low (e.g. < 10%) or empty
        return drone.getHomePos() != null && drone.getEnergy() < (DroneEntity.MAX_ENERGY * 0.1);
    }

    @Override
    public void start() {
        // Optional: Play "Low battery" sound
    }

    @Override
    public void tick() {
        BlockPos home = drone.getHomePos();
        if (home != null) {
            // Move towards top of bay
            drone.getNavigation().moveTo(home.getX() + 0.5, home.getY() + 1.2, home.getZ() + 0.5, 1.0D);

            if (drone.distanceToSqr(home.getX() + 0.5, home.getY() + 1.2, home.getZ() + 0.5) < 2.0) {
                drone.setEnergy(DroneEntity.MAX_ENERGY); // Recharge instantly (or increment slowly)
            }
        }
    }
}
