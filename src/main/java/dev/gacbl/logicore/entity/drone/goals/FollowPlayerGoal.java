package dev.gacbl.logicore.entity.drone.goals;

import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    private final DroneEntity drone;
    private Player owner;

    public FollowPlayerGoal(DroneEntity drone) {
        this.drone = drone;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.owner = drone.getOwner();
        return owner != null && !owner.isSpectator() && drone.getCyclesStored() > 0 && drone.distanceToSqr(owner) > 10.0;
    }

    @Override
    public void tick() {
        if (owner == null) return;

        drone.getLookControl().setLookAt(owner, 10.0F, (float) drone.getMaxHeadXRot());

        // Target position: Behind an owner and slightly up
        double targetX = owner.getX() - (owner.getLookAngle().x * 1.2);
        double targetZ = owner.getZ() - (owner.getLookAngle().z * 1.2);
        double targetY = owner.getY() + 3.5;


        if (drone.distanceToSqr(targetX, targetY, targetZ) > 200D) {
            drone.teleportTo(targetX, targetY, targetZ);
        }

        drone.getNavigation().moveTo(targetX, targetY, targetZ, 1.7);
    }
}
