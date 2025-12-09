package dev.gacbl.logicore.entity.drone.goals;

import dev.gacbl.logicore.entity.drone.DroneEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public class HealOwnerGoal extends Goal {
    private final DroneEntity drone;
    private int cooldown;
    private final int cost;
    private int defaultCooldown = 0;

    public HealOwnerGoal(DroneEntity drone, int cooldown, int cost) {
        this.drone = drone;
        this.cooldown = 0;
        this.cost = cost;
        this.defaultCooldown = cooldown;
    }

    @Override
    public boolean canUse() {
        Player owner = drone.getOwner();
        return owner != null && owner.getHealth() < owner.getMaxHealth() && drone.getCyclesStored() >= cost;
    }

    @Override
    public void start() {
        cooldown = defaultCooldown;
    }

    @Override
    public void tick() {
        Player owner = drone.getOwner();
        if (cooldown <= 0 && owner != null && drone.distanceToSqr(owner) < 36.0) {
            owner.heal(4.0f); // Heal 2 hearts

            drone.getCycleStorage().extractCycles(cost, false);
            drone.level().broadcastEntityEvent(drone, (byte) 60);
            cooldown = defaultCooldown;
        }
        if (cooldown > 0) cooldown--;
    }
}
