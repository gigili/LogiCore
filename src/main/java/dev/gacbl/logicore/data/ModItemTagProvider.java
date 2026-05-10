package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.items.pickaxe.CyclePickModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider, LogiCore.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench"))).add(WrenchModule.WRENCH.get());
        tag(ItemTags.PICKAXES).add(CyclePickModule.CYCLE_PICK.get());
        tag(ItemTags.MINING_ENCHANTABLE).add(CyclePickModule.CYCLE_PICK.get());
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(CyclePickModule.CYCLE_PICK.get());
    }
}
