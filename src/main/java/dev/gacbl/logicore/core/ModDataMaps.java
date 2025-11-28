package dev.gacbl.logicore.core;

import com.mojang.serialization.Codec;
import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class ModDataMaps {
    public static final DataMapType<Item, Integer> ITEM_CYCLES = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "item_cycles"),
            Registries.ITEM,
            Codec.INT
    ).synced(Codec.INT, true).build();
}
