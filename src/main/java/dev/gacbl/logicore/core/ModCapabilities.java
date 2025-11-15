package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleProvider;
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

    public static final BlockCapability<ICycleProvider, Direction> CYCLE_PROVIDER =
            BlockCapability.create(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_provider"),
                    ICycleProvider.class,
                    Direction.class
            );

    public static final BlockCapability<ICycleConsumer, Direction> CYCLE_CONSUMER =
            BlockCapability.create(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_consumer"),
                    ICycleConsumer.class,
                    Direction.class
            );
}
