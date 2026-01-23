package dev.gacbl.logicore.blocks.repair_station;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RepairStationLootTableProvider extends BlockLootSubProvider {
    public RepairStationLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(RepairStationModule.REPAIR_STATION.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return RepairStationModule.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
