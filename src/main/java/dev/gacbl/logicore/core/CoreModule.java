package dev.gacbl.logicore.core;

import dev.gacbl.logicore.cpucore.CPUCoreModule;
import dev.gacbl.logicore.serverrack.ServerRackModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CoreModule {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CoreModule::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );

        // Attach Cycle Storage capability to CPU Core
        event.registerBlockEntity(
                ModCapabilities.CYCLE_STORAGE,
                CPUCoreModule.CPU_CORE_BLOCK_ENTITY.get(), // Uncommented
                (be, context) -> be.getCycleStorage()     // Uncommented
        );

        // Attach Energy capability to CPU Core
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CPUCoreModule.CPU_CORE_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyStorage()
        );

        // Attach Item Handler capability to Server Rack
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ServerRackModule.SERVER_RACK_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );
    }
}
