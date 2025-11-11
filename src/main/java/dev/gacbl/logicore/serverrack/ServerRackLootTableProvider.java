package dev.gacbl.logicore.serverrack;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ServerRackLootTableProvider  extends BlockLootSubProvider {

    public ServerRackLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        this.add(
                ServerRackModule.SERVER_RACK_BLOCK.get(),
                block -> createSinglePropConditionTable(
                        block, BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER
                )
        );
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ServerRackModule.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
