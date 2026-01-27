package dev.gacbl.logicore.enchantment;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.core.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = LogiCore.MOD_ID)
public class RecyclerEnchantmentModule {
    public static final ResourceKey<Enchantment> RECYCLER = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "recycler"));

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) return;

        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty()) return;

        ServerLevel level = player.serverLevel();
        var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var recyclerEnchantment = enchantmentRegistry.getOrThrow(RECYCLER);

        if (EnchantmentHelper.getTagEnchantmentLevel(recyclerEnchantment, tool) <= 0) return;

        if (event.getState().is(ModTags.Blocks.RECYCLER_BLACKLIST)) return;

        ItemStack dropStack = new ItemStack(event.getState().getBlock());
        if (CycleValueManager.hasCycleValue(dropStack)) {
            int cyclesValue = CycleValueManager.getCycleValue(dropStack);

            // Respect Fortune/Looting
            int bonusLevel = EnchantmentHelper.getTagEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.FORTUNE), tool);
            if (bonusLevel == 0) {
                bonusLevel = EnchantmentHelper.getTagEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.LOOTING), tool);
            }

            if (bonusLevel > 0) {
                cyclesValue *= (bonusLevel + 1);
            }

            String key = CycleSavedData.getKey(level, player.getUUID());
            CycleSavedData.get(level).modifyCycles(level, key, cyclesValue);

            event.setCanceled(true);
            level.destroyBlock(event.getPos(), false);
            level.levelEvent(null, 2001, event.getPos(), Block.getId(event.getState()));
        }
    }
}
