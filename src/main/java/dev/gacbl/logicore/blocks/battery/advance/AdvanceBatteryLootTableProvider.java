package dev.gacbl.logicore.blocks.battery.advance;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AdvanceBatteryLootTableProvider extends BlockLootSubProvider {
    public AdvanceBatteryLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(AdvanceBatteryModule.ADVANCE_BATTERY.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return AdvanceBatteryModule.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
