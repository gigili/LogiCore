package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class ModTiers {
    public static final ToolMaterial CYCLE_PICK = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            3000,
            12.0F,
            4.0F,
            15,
            ItemTags.create(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_pick_repair"))
    );
}
