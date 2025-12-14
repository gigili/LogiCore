package dev.gacbl.logicore.items.wrench;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, WrenchModule.WRENCH.get())
                .pattern(" QP")
                .pattern(" SI")
                .pattern("S  ")
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .define('Q', Items.QUARTZ)
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT.get());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (level.isClientSide || player == null) return InteractionResult.SUCCESS;
        return InteractionResult.PASS;
    }

    private Direction getClickedDirection(Vec3 localHit) {
        Vec3 center = new Vec3(0.5, 0.5, 0.5);
        Vec3 dirVec = localHit.subtract(center);
        return Direction.getNearest(dirVec.x, dirVec.y, dirVec.z);
    }
}
