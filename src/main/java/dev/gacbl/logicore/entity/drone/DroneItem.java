package dev.gacbl.logicore.entity.drone;

import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlockEntity;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DroneItem extends Item {
    public DroneItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, DroneModule.DRONE_ITEM.get())
                .pattern("EGE")
                .pattern("RPR")
                .pattern("EGE")
                .define('E', Items.ENDER_PEARL)
                .define('R', Items.REDSTONE)
                .define('G', Items.GOLD_INGOT)
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos foundBayPos = null;
        double minDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(clickedPos.offset(-4, -4, -4), clickedPos.offset(4, 4, 4))) {
            if (level.getBlockEntity(pos) instanceof DroneBayBlockEntity) {
                double dist = pos.distSqr(clickedPos);
                if (dist < minDistance) {
                    minDistance = dist;
                    foundBayPos = pos.immutable();
                }
            }
        }

        if (foundBayPos == null) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("errors.logicore.drone.no_drone_bay_nearby"), true);
            }
            return InteractionResult.FAIL;
        }

        // 3. Spawn Drone
        BlockPos spawnPos = clickedPos.above();
        DroneEntity drone = DroneModule.DRONE.get().create(serverLevel);

        if (drone != null) {
            drone.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

            // Set Custom Data
            drone.setHomePos(foundBayPos);
            if (!context.getItemInHand().getHoverName().getString().isEmpty()) {
                drone.setCustomName(context.getItemInHand().getHoverName());
            }

            if (context.getPlayer() != null) {
                drone.setOwnerUUID(context.getPlayer().getUUID());
            }

            serverLevel.addFreshEntity(drone);
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
