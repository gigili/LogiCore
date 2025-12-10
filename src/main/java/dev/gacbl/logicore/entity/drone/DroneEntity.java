package dev.gacbl.logicore.entity.drone;

import dev.gacbl.logicore.api.computation.CycleStorage;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlockEntity;
import dev.gacbl.logicore.entity.drone.goals.FollowPlayerGoal;
import dev.gacbl.logicore.entity.drone.goals.HealOwnerGoal;
import dev.gacbl.logicore.entity.drone.goals.RechargeIfEmptyGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class DroneEntity extends FlyingMob implements ICycleConsumer {
    private static final EntityDataAccessor<Long> CYCLES = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private final CycleStorage cycleStorage = new CycleStorage(1000, 100, 100);

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    protected DroneEntity(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CYCLES, 0L);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(HOME_POS, Optional.empty());
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10d)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RechargeIfEmptyGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new HealOwnerGoal(this, 20, 50));
        this.goalSelector.addGoal(3, new FollowPlayerGoal(this));

        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public void setHomePos(BlockPos pos) {
        this.entityData.set(HOME_POS, Optional.ofNullable(pos));
    }

    public BlockPos getHomePos() {
        return this.entityData.get(HOME_POS).orElse(null);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public Player getOwner() {
        Optional<UUID> uuid = this.entityData.get(OWNER_UUID);
        return uuid.map(value -> this.level().getPlayerByUUID(value)).orElse(null);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("cycles", cycleStorage.getCyclesStored());
        if (getHomePos() != null) tag.putLong("HomePos", getHomePos().asLong());
        this.entityData.get(OWNER_UUID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        cycleStorage.setCycles(tag.getLong("cycles"));
        if (tag.contains("HomePos")) setHomePos(BlockPos.of(tag.getLong("HomePos")));
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        if (level.isClientSide) return;
        ItemStack stack = new ItemStack(DroneModule.DRONE_ITEM.get());
        if (this.hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        this.spawnAtLocation(stack);

        if (getHomePos() == null) return;
        if (level.getBlockEntity(getHomePos()) instanceof DroneBayBlockEntity droneBayBlockEntity) {
            droneBayBlockEntity.clearDockedName();
        }
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public long getCycleDemand() {
        return cycleStorage.getCycleDemand();
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        return cycleStorage.receiveCycles(maxReceive, simulate);
    }

    @Override
    public long extractCycles(long maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public long getCyclesStored() {
        return cycleStorage.getCyclesStored();
    }

    public CycleStorage getCycleStorage() {
        return cycleStorage;
    }
}
