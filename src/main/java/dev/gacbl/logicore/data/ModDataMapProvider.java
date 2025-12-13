package dev.gacbl.logicore.data;

import dev.gacbl.logicore.core.ModDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
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
        Builder<Integer, Item> builder = builder(ModDataMaps.ITEM_CYCLES);

        addBase(builder, Items.DIRT_PATH, 1);
        addBase(builder, Items.SOUL_SOIL, 4);
        addBase(builder, Items.HONEYCOMB, 18);
        addBase(builder, Items.HONEY_BLOCK, 24);
        addBase(builder, Items.AMETHYST_SHARD, 16);

        addTag(builder, ItemTags.SAND, 1);
        addTag(builder, ItemTags.DIRT, 1);
        addTag(builder, Tags.Items.GRAVELS, 1);
        addTag(builder, Tags.Items.COBBLESTONES, 1);
        addTag(builder, Tags.Items.STONES, 2);
        addTag(builder, Tags.Items.DYES, 1);
        addTag(builder, Tags.Items.CONCRETES, 9);
        addTag(builder, Tags.Items.GLASS_BLOCKS, 1);
        addTag(builder, Tags.Items.GLASS_PANES, 6);

        // --- FOOD & FARMING ---
        addTag(builder, Tags.Items.ANIMAL_FOODS, 16);
        addTag(builder, Tags.Items.CROPS, 12);
        addTag(builder, Tags.Items.FOODS, 12);
        addTag(builder, Tags.Items.MUSHROOMS, 16);
        addTag(builder, Tags.Items.EGGS, 16);

        // --- MOB DROPS & MISC ---
        addBase(builder, Items.GLOW_INK_SAC, 48);
        addBase(builder, Items.NETHER_STAR, 130000);
        addBase(builder, Items.DRAGON_BREATH, 150000);

        addTag(builder, Tags.Items.SLIME_BALLS, 8);
        addTag(builder, Tags.Items.STRINGS, 12);
        addTag(builder, Tags.Items.LEATHERS, 16);
        addTag(builder, Tags.Items.FEATHERS, 16);
        addTag(builder, Tags.Items.BONES, 16);
        addTag(builder, Tags.Items.ENDER_PEARLS, 1024);
        addTag(builder, Tags.Items.GUNPOWDERS, 192);

        // --- ORES & METALS ---
        addTag(builder, ItemTags.COALS, 16);
        addTag(builder, Tags.Items.INGOTS_IRON, 256);
        addTag(builder, Tags.Items.INGOTS_GOLD, 2048);
        addTag(builder, Tags.Items.INGOTS_COPPER, 64);
        addTag(builder, Tags.Items.GEMS_LAPIS, 864);
        addTag(builder, Tags.Items.OBSIDIANS, 64);

        // NEW VALUES
        addBase(builder, Items.COBBLESTONE, 1);
        addBase(builder, Items.GRANITE, 16);
        addBase(builder, Items.DIORITE, 16);
        addBase(builder, Items.ANDESITE, 16);
        addBase(builder, Items.POINTED_DRIPSTONE, 16);
        addBase(builder, Items.END_STONE, 1);
        addBase(builder, Items.NETHERRACK, 1);
        addBase(builder, Items.BASALT, 4);
        addBase(builder, Items.BLACKSTONE, 4);
        addBase(builder, Items.COBBLED_DEEPSLATE, 2);
        addBase(builder, Items.TUFF, 4);
        addBase(builder, Items.CALCITE, 32);
        addBase(builder, Items.DIRT, 1);
        addBase(builder, Items.SAND, 1);
        addBase(builder, Items.RED_SAND, 1);
        addBase(builder, Items.SNOW, 1);
        addBase(builder, Items.ICE, 1);
        addBase(builder, Items.DEAD_BUSH, 1);
        addBase(builder, Items.GRAVEL, 4);
        addBase(builder, Items.CACTUS, 8);
        addBase(builder, Items.VINE, 8);
        addBase(builder, Items.MOSS_BLOCK, 12);
        addBase(builder, Items.COBWEB, 12);
        addBase(builder, Items.PINK_PETALS, 4);
        addBase(builder, Items.LILY_PAD, 16);
        addBase(builder, Items.SMALL_DRIPLEAF, 24);
        addBase(builder, Items.BIG_DRIPLEAF, 32);
        addTag(builder, ItemTags.SMALL_FLOWERS, 16);
        addTag(builder, ItemTags.TALL_FLOWERS, 32);
        addBase(builder, Items.RED_MUSHROOM, 32);
        addBase(builder, Items.BROWN_MUSHROOM, 32);
        addBase(builder, Items.SUGAR_CANE, 32);
        addBase(builder, Items.BAMBOO, 32);
        addBase(builder, Items.SOUL_SAND, 49);
        addBase(builder, Items.OBSIDIAN, 64);
        addBase(builder, Items.CRYING_OBSIDIAN, 768);
        addBase(builder, Items.SPONGE, 128);
        addBase(builder, Items.SHORT_GRASS, 1);
        addBase(builder, Items.SEAGRASS, 1);
        addBase(builder, Items.KELP, 1);
        addBase(builder, Items.SEA_PICKLE, 16);
        addBase(builder, Items.TALL_GRASS, 1);
        addBase(builder, Items.FERN, 1);
        addBase(builder, Items.LARGE_FERN, 1);
        addBase(builder, Items.MAGMA_BLOCK, 128);
        addBase(builder, Items.NETHER_SPROUTS, 1);
        addBase(builder, Items.CRIMSON_ROOTS, 1);
        addBase(builder, Items.WARPED_ROOTS, 1);
        addBase(builder, Items.WEEPING_VINES, 8);
        addBase(builder, Items.TWISTING_VINES, 8);
        addBase(builder, Items.GLOW_LICHEN, 8);
        addBase(builder, Items.CRIMSON_FUNGUS, 32);
        addBase(builder, Items.WARPED_FUNGUS, 32);
        addBase(builder, Items.SPORE_BLOSSOM, 64);
        addBase(builder, Items.TUBE_CORAL_BLOCK, 64);
        addBase(builder, Items.BRAIN_CORAL_BLOCK, 64);
        addBase(builder, Items.BUBBLE_CORAL_BLOCK, 64);
        addBase(builder, Items.FIRE_CORAL_BLOCK, 64);
        addBase(builder, Items.HORN_CORAL_BLOCK, 64);
        addBase(builder, Items.DEAD_TUBE_CORAL_BLOCK, 4);
        addBase(builder, Items.DEAD_BRAIN_CORAL_BLOCK, 4);
        addBase(builder, Items.DEAD_BUBBLE_CORAL_BLOCK, 4);
        addBase(builder, Items.DEAD_FIRE_CORAL_BLOCK, 4);
        addBase(builder, Items.DEAD_HORN_CORAL_BLOCK, 4);
        addBase(builder, Items.TUBE_CORAL_FAN, 16);
        addBase(builder, Items.BRAIN_CORAL_FAN, 16);
        addBase(builder, Items.BUBBLE_CORAL_FAN, 16);
        addBase(builder, Items.FIRE_CORAL_FAN, 16);
        addBase(builder, Items.HORN_CORAL_FAN, 16);
        addBase(builder, Items.DEAD_TUBE_CORAL_FAN, 1);
        addBase(builder, Items.DEAD_BRAIN_CORAL_FAN, 1);
        addBase(builder, Items.DEAD_BUBBLE_CORAL_FAN, 1);
        addBase(builder, Items.DEAD_FIRE_CORAL_FAN, 1);
        addBase(builder, Items.DEAD_HORN_CORAL_FAN, 1);
        addBase(builder, Items.TUBE_CORAL, 16);
        addBase(builder, Items.BRAIN_CORAL, 16);
        addBase(builder, Items.BUBBLE_CORAL, 16);
        addBase(builder, Items.FIRE_CORAL, 16);
        addBase(builder, Items.HORN_CORAL, 16);
        addBase(builder, Items.DEAD_TUBE_CORAL, 1);
        addBase(builder, Items.DEAD_BRAIN_CORAL, 1);
        addBase(builder, Items.DEAD_BUBBLE_CORAL, 1);
        addBase(builder, Items.DEAD_FIRE_CORAL, 1);
        addBase(builder, Items.DEAD_HORN_CORAL, 1);
        addBase(builder, Items.CHORUS_PLANT, 64);
        addBase(builder, Items.CHORUS_FLOWER, 96);
        addBase(builder, Items.CHORUS_FRUIT, 192);
        addBase(builder, Items.SCULK_VEIN, 4);
        addBase(builder, Items.SCULK_CATALYST, 8_040);
        addTag(builder, Tags.Items.SEEDS_WHEAT, 16);
        addTag(builder, Tags.Items.SEEDS_BEETROOT, 16);
        addBase(builder, Items.MELON_SLICE, 16);
        addBase(builder, Items.SWEET_BERRIES, 16);
        addBase(builder, Items.GLOW_BERRIES, 16);
        addTag(builder, Tags.Items.CROPS_WHEAT, 24);
        addTag(builder, Tags.Items.CROPS_NETHER_WART, 24);
        addBase(builder, Items.APPLE, 128);
        addTag(builder, Tags.Items.PUMPKINS_NORMAL, 144);
        addBase(builder, Items.HONEY_BOTTLE, 48);
        addBase(builder, Items.PORKCHOP, 64);
        addBase(builder, Items.BEEF, 64);
        addBase(builder, Items.CHICKEN, 64);
        addBase(builder, Items.RABBIT, 64);
        addBase(builder, Items.MUTTON, 64);
        addBase(builder, Items.COD, 64);
        addBase(builder, Items.SALMON, 64);
        addBase(builder, Items.TROPICAL_FISH, 64);
        addBase(builder, Items.PUFFERFISH, 64);
        addTag(builder, Tags.Items.CROPS_CARROT, 64);
        addTag(builder, Tags.Items.CROPS_BEETROOT, 64);
        addTag(builder, Tags.Items.CROPS_POTATO, 64);
        addBase(builder, Items.POISONOUS_POTATO, 64);
        addBase(builder, Items.STRING, 12);
        addBase(builder, Items.ROTTEN_FLESH, 32);
        addBase(builder, Items.SLIME_BALL, 32);
        addBase(builder, Items.EGG, 32);
        addBase(builder, Items.TURTLE_SCUTE, 96);
        addBase(builder, Items.TURTLE_EGG, 192);
        //Regular horns
        addBase(builder, horn(provider, Instruments.PONDER_GOAT_HORN).getItem(), 96);
        addBase(builder, horn(provider, Instruments.SING_GOAT_HORN).getItem(), 96);
        addBase(builder, horn(provider, Instruments.SEEK_GOAT_HORN).getItem(), 96);
        addBase(builder, horn(provider, Instruments.FEEL_GOAT_HORN).getItem(), 96);
        //Screaming horns
        addBase(builder, horn(provider, Instruments.ADMIRE_GOAT_HORN).getItem(), 192);
        addBase(builder, horn(provider, Instruments.CALL_GOAT_HORN).getItem(), 192);
        addBase(builder, horn(provider, Instruments.YEARN_GOAT_HORN).getItem(), 192);
        addBase(builder, horn(provider, Instruments.DREAM_GOAT_HORN).getItem(), 192);
        addBase(builder, Items.FEATHER, 48);
        addBase(builder, Items.RABBIT_HIDE, 16);
        addBase(builder, Items.RABBIT_FOOT, 128);
        addBase(builder, Items.SPIDER_EYE, 128);
        addBase(builder, Items.PHANTOM_MEMBRANE, 192);
        addBase(builder, Items.GUNPOWDER, 192);
        addBase(builder, Items.SKELETON_SKULL, 256);
        addBase(builder, Items.ZOMBIE_HEAD, 256);
        addBase(builder, Items.CREEPER_HEAD, 256);
        addBase(builder, Items.PIGLIN_HEAD, 256);
        addBase(builder, Items.PIGLIN_BANNER_PATTERN, 512);
        addBase(builder, Items.FLOW_BANNER_PATTERN, 20_480);
        addBase(builder, Items.GUSTER_BANNER_PATTERN, 12_224);
        addBase(builder, Items.ENDER_PEARL, 1_024);
        addBase(builder, Items.NAUTILUS_SHELL, 1_024);
        addTag(builder, Tags.Items.RODS_BLAZE, 1_536);
        addTag(builder, Tags.Items.RODS_BREEZE, 2_304);
        addBase(builder, Items.SHULKER_SHELL, 2_048);
        addBase(builder, Items.SNIFFER_EGG, 2_048);
        addBase(builder, Items.GHAST_TEAR, 4_096);
        addBase(builder, Items.TRIDENT, 16_398);
        addBase(builder, Items.HEART_OF_THE_SEA, 32_768);
        addBase(builder, Items.HEAVY_CORE, 40_960);
        addBase(builder, Items.DRAGON_EGG, 262_144);
        addBase(builder, Items.SADDLE, 192);
        addBase(builder, Items.ECHO_SHARD, 192);
        addBase(builder, Items.NAME_TAG, 192);
        addTag(builder, ItemTags.CREEPER_DROP_MUSIC_DISCS, 2_048);
        addBase(builder, Items.DISC_FRAGMENT_5, 192);
        addBase(builder, Items.MUSIC_DISC_CREATOR, 40_960);
        addBase(builder, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, 8_192);
        addBase(builder, Items.MUSIC_DISC_OTHERSIDE, 6_144);
        addBase(builder, Items.MUSIC_DISC_PIGSTEP, 8_192);
        addBase(builder, Items.MUSIC_DISC_PRECIPICE, 12_224);
        addBase(builder, Items.MUSIC_DISC_RELIC, 10_176);
        addBase(builder, Items.FLINT, 4);
        addBase(builder, Items.COAL, 128);
        addTag(builder, Tags.Items.GEMS_QUARTZ, 256);
        addBase(builder, Items.PRISMARINE_SHARD, 256);
        addBase(builder, Items.PRISMARINE_CRYSTALS, 512);
        addBase(builder, Items.INK_SAC, 16);
        addBase(builder, Items.COCOA_BEANS, 64);
        addBase(builder, Items.LAPIS_LAZULI, 864);
        addTag(builder, Tags.Items.GEMS_EMERALD, 16_384);
        addTag(builder, Tags.Items.NETHER_STARS, 139_264);
        addBase(builder, Items.CLAY_BALL, 16);
        addTag(builder, ItemTags.DECORATED_POT_SHERDS, 216);
        addBase(builder, Items.BONE, 144);
        addBase(builder, Items.SNOWBALL, 1);
        addBase(builder, Items.FILLED_MAP, 1_472);
        addTag(builder, ItemTags.LOGS, 32);
        addTag(builder, ItemTags.PLANKS, 8);
        addTag(builder, ItemTags.SAPLINGS, 32);
        addTag(builder, Tags.Items.RODS_WOODEN, 4);
        addTag(builder, ItemTags.LEAVES, 1);
        addBase(builder, Items.MANGROVE_ROOTS, 4);
        addTag(builder, ItemTags.WOOL, 48);
        addBase(builder, Items.ARMADILLO_SCUTE, 48);
        addBase(builder, Items.NETHERITE_SCRAP, 12_288);
        addTag(builder, Tags.Items.GEMS_DIAMOND, 8_192);
        addTag(builder, Tags.Items.DUSTS_REDSTONE, 64);
        addTag(builder, Tags.Items.DUSTS_GLOWSTONE, 384);
        addBase(builder, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 7_497);
        addBase(builder, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 23_017);
        addBase(builder, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 57_345);
        addBase(builder, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 53_898);
        addBase(builder, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 51_917);
        addBase(builder, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 42_641);
        addBase(builder, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 39_465);
        addBase(builder, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 22_116);
        addBase(builder, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 19_677);
        addBase(builder, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 18_588);
        addBase(builder, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 12_271);
        addBase(builder, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 10_310);
        addBase(builder, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176);
        addBase(builder, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176);
        addBase(builder, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176);
        addBase(builder, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176);
        addBase(builder, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 7_533);
        addBase(builder, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, 30_528);
        addBase(builder, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176);
    }

    private ItemStack horn(HolderLookup.Provider registries, ResourceKey<Instrument> instrument) {
        return InstrumentItem.create(Items.GOAT_HORN, registries.holderOrThrow(instrument));
    }

    private void addBase(Builder<Integer, Item> builder, Item item, int value) {
        builder.add(BuiltInRegistries.ITEM.wrapAsHolder(item), value, false);
    }

    private void addTag(Builder<Integer, Item> builder, TagKey<Item> tag, int value) {
        builder.add(tag, value, false);
    }
}
