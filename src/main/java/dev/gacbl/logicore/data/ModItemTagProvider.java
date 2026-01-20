package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.items.pickaxe.CyclePickModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTags, LogiCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"))).add(WrenchModule.WRENCH.get());
        tag(ItemTags.PICKAXES).add(CyclePickModule.CYCLE_PICK.get());
        tag(ItemTags.create(ResourceLocation.withDefaultNamespace("enchantable/mining_tool"))).add(CyclePickModule.CYCLE_PICK.get());
        tag(ItemTags.create(ResourceLocation.withDefaultNamespace("enchantable/durability"))).add(CyclePickModule.CYCLE_PICK.get());
    }
}
