package dev.gacbl.logicore.blocks.datacenter;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DataCenterControllerLootTableProvider extends BlockLootSubProvider {

    public DataCenterControllerLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(DatacenterModule.DATACENTER_CONTROLLER.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return DatacenterModule.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
