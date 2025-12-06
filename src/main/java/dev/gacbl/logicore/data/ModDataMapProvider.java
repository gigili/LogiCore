package dev.gacbl.logicore.data;

import dev.gacbl.logicore.core.ModDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {
    protected ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.@NotNull Provider provider) {
        builder(ModDataMaps.ITEM_CYCLES)
                .add(ItemTags.LOGS, 32, false)
                .add(Tags.Items.RODS_WOODEN, 4, false)
                .add(ItemTags.SAND, 1, false)
                .add(ItemTags.DIRT, 1, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIRT_PATH), 1, false)
                .add(Tags.Items.GRAVELS, 1, false)
                .add(Tags.Items.COBBLESTONES, 1, false)
                .add(Tags.Items.STONES, 2, false)
                .add(Tags.Items.DYES, 1, false)
                .add(Tags.Items.CONCRETES, 9, false)
                .add(Tags.Items.ANIMAL_FOODS, 16, false)
                .add(Tags.Items.CROPS, 12, false)
                .add(Tags.Items.FOODS, 12, false)
                .add(Tags.Items.INGOTS_IRON, 256, false)
                .add(Tags.Items.INGOTS_GOLD, 512, false)
                .add(Tags.Items.GEMS_DIAMOND, 1024, false)
                .add(Tags.Items.GEMS_EMERALD, 1056, false)
                .add(Tags.Items.DUSTS_REDSTONE, 64, false)
                .add(Tags.Items.GUNPOWDERS, 16, false)
                .add(Tags.Items.DUSTS_GLOWSTONE, 16, false)
                .add(Tags.Items.GEMS_LAPIS, 16, false)
                .add(Tags.Items.SLIME_BALLS, 8, false)
                .add(Tags.Items.STRINGS, 2, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.SOUL_SAND), 4, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.SOUL_SOIL), 4, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHER_STAR), 130000, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.DRAGON_EGG), 250000, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.DRAGON_BREATH), 150000, false)
                .add(ItemTags.COALS, 16, false)
                .add(Tags.Items.INGOTS_COPPER, 64, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.FLINT), 4, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.CLAY_BALL), 8, false)
                .add(Tags.Items.OBSIDIANS, 64, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHERITE_SCRAP), 2056, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHERRACK), 1, false)
                .add(Tags.Items.GEMS_QUARTZ, 32, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.SOUL_SAND), 4, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.END_STONE), 4, false)
                .add(Tags.Items.ENDER_PEARLS, 1024, false)
                .add(Tags.Items.LEATHERS, 16, false)
                .add(Tags.Items.FEATHERS, 16, false)
                .add(Tags.Items.BONES, 16, false)
                .add(Tags.Items.RODS_BLAZE, 256, false)
                .add(Tags.Items.MUSHROOMS, 16, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.INK_SAC), 24, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.GLOW_INK_SAC), 48, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.HONEYCOMB), 18, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.AMETHYST_SHARD), 16, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.HONEY_BLOCK), 24, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.GHAST_TEAR), 256, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.PHANTOM_MEMBRANE), 1024, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.RABBIT_FOOT), 256, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.HEART_OF_THE_SEA), 512, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.NAUTILUS_SHELL), 256, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.ICE), 18, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.SNOWBALL), 6, false)
                .add(Tags.Items.GLASS_BLOCKS, 1, false)
                .add(Tags.Items.GLASS_PANES, 6, false);
    }
}
