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
                .add(Tags.Items.STONES, 2, false)
                .add(Tags.Items.ANIMAL_FOODS, 32, false)
                .add(Tags.Items.CONCRETES, 8, false)
                .add(Tags.Items.CROPS, 12, false)
                .add(Tags.Items.DUSTS, 64, false)
                .add(Tags.Items.GEMS, 3072, false)
                .add(Tags.Items.ORES_IRON, 128, false)
                .add(Tags.Items.INGOTS_IRON, 256, false)
                .add(Tags.Items.ORES_GOLD, 1024, false)
                .add(Tags.Items.INGOTS_GOLD, 2048, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHER_STAR), 130000, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.DRAGON_EGG), 250000, false)
                .add(BuiltInRegistries.ITEM.wrapAsHolder(Items.DRAGON_BREATH), 150000, false);
    }
}
