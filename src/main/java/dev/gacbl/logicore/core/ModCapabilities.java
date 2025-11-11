package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class ModCapabilities {
    public static final BlockCapability<ICycleStorage, Direction> CYCLE_STORAGE =
            BlockCapability.create(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_storage"),
                    ICycleStorage.class,
                    Direction.class
            );
}
