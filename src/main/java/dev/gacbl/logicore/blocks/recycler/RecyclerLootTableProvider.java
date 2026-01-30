package dev.gacbl.logicore.blocks.recycler;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RecyclerLootTableProvider extends BlockLootSubProvider {
    public RecyclerLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(RecyclerModule.RECYCLER.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return RecyclerModule.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
