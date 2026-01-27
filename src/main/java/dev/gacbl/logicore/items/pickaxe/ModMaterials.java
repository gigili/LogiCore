package dev.gacbl.logicore.items.pickaxe;

import dev.gacbl.logicore.core.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public class ModMaterials {
    public static final ToolMaterial CYCLE_PICK = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            3000,
            12.0F,
            4.0F,
            15,
            ModTags.Items.REPAIR_TOOLS
    );
}
