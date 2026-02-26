package dev.gacbl.logicore.items.processorunit;

import net.minecraft.world.item.Item;

public class ProcessorUnitItem extends Item {
    public final ProcessorUnitTier tier;

    public ProcessorUnitItem(ProcessorUnitTier tier) {
        super(new Item.Properties());
        this.tier = tier;
    }
}
