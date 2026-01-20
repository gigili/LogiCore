package dev.gacbl.logicore.data;

import dev.gacbl.logicore.enchantment.RecyclerEnchantmentModule;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantmentProvider {
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        register(context, RecyclerEnchantmentModule.RECYCLER, Enchantment.enchantment(
                Enchantment.definition(
                        items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                        5,
                        1,
                        Enchantment.constantCost(15),
                        Enchantment.constantCost(65),
                        4,
                        EquipmentSlotGroup.MAINHAND
                ))
        );
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }
}
