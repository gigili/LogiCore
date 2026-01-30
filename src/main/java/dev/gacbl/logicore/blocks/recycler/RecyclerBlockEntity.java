package dev.gacbl.logicore.blocks.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;

public class RecyclerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public RecyclerBlockEntity(BlockPos pos, BlockState blockState) {
        super(RecyclerModule.RECYCLER_BE.get(), pos, blockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<RecyclerBlockEntity> recyclerBlockEntityAnimationState) {
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            BlockEntity entity = level.getBlockEntity(worldPosition);
            if (entity instanceof RecyclerBlockEntity && state.getValue(RecyclerModule.CRUSHING)) {
                recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("crushing", Animation.LoopType.LOOP));
            } else {
                recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
            }
        } else {
            recyclerBlockEntityAnimationState.setAndContinue(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
