package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModTiers {
    public static final Tier CYCLE_PICK = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            3000,
            12.0F,
            4.0F,
            15,
            () -> Ingredient.of(ProcessorUnitModule.PROCESSOR_UNIT.get())
    );
}
