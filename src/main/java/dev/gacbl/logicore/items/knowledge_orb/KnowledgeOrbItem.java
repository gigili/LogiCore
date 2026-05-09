package dev.gacbl.logicore.items.knowledge_orb;

import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KnowledgeOrbItem extends Item {
    public KnowledgeOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            String key = CycleSavedData.getKey(serverLevel, player.getUUID());
            CycleSavedData data = CycleSavedData.get(serverLevel);

            for (Item item : CycleValueManager.CYCLE_VALUES.keySet()) {
                data.unlockItem(serverLevel, key, new ItemStack(item));
            }

            stack.shrink(1);
            player.sendSystemMessage(Component.translatable("item.logicore.knowledge_orb.unlocked_all"));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.item.logicore.knowledge_orb"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
