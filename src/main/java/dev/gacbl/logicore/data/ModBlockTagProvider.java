package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, LogiCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ServerRackModule.SERVER_RACK_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(DataCableModule.DATA_CABLE_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ComputerModule.COMPUTER_BLOCK.get());
    }
}
