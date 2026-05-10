package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.api.computation.ICycleProvider;
import dev.gacbl.logicore.api.computation.ICycleStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class ModCapabilities {
    public static final BlockCapability<ICycleStorage, Direction> CYCLE_STORAGE =
            BlockCapability.create(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_storage"),
                    ICycleStorage.class,
                    Direction.class
            );

    public static final BlockCapability<ICycleProvider, Direction> CYCLE_PROVIDER =
            BlockCapability.create(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_provider"),
                    ICycleProvider.class,
                    Direction.class
            );

    public static final BlockCapability<ICycleConsumer, Direction> CYCLE_CONSUMER =
            BlockCapability.create(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_consumer"),
                    ICycleConsumer.class,
                    Direction.class
            );

    public static final EntityCapability<ICycleConsumer, Direction> ENTITY_CYCLE_CONSUMER =
            EntityCapability.create(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "entity_cycle_consumer"),
                    ICycleConsumer.class,
                    Direction.class
            );
}
