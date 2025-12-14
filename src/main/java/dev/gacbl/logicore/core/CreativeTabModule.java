package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayModule;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.entity.drone.DroneModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabModule {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LogiCore.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> LOGICORE_TAB =
            CREATIVE_MODE_TABS.register("logicore_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.logicore.tab"))
                    .icon(() -> new ItemStack(ProcessorUnitModule.PROCESSOR_UNIT.get()))
                    .displayItems((params, output) -> {
                        output.accept(ServerRackModule.SERVER_RACK_ITEM.get());
                        output.accept(DataCableModule.DATA_CABLE_ITEM.get());
                        output.accept(ProcessorUnitModule.PROCESSOR_UNIT.get());
                        output.accept(ComputerModule.COMPUTER_ITEM.get());
                        output.accept(DatacenterModule.DATACENTER_CONTROLLER.get());
                        output.accept(CompilerModule.COMPILER_ITEM.get());
                        output.accept(DatacenterPortModule.DATACENTER_PORT.get());
                        output.accept(DroneBayModule.DRONE_BAY.get());
                        output.accept(DroneModule.DRONE_ITEM.get());
                        output.accept(GeneratorModule.GENERATOR.get());
                        output.accept(CloudInterfaceModule.CLOUD_INTERFACE.get());
                        output.accept(WrenchModule.WRENCH.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
