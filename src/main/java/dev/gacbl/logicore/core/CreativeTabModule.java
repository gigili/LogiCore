package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.computer.ComputerModule;
import dev.gacbl.logicore.cpucore.CPUCoreModule;
import dev.gacbl.logicore.datacable.DataCableModule;
import dev.gacbl.logicore.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.serverrack.ServerRackModule;
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
                    .icon(() -> new ItemStack(CPUCoreModule.CPU_CORE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(CPUCoreModule.CPU_CORE_ITEM.get());
                        output.accept(ServerRackModule.SERVER_RACK_ITEM.get());
                        output.accept(DataCableModule.DATA_CABLE_ITEM.get());
                        output.accept(ProcessorUnitModule.PROCESSOR_UNIT.get());
                        output.accept(ComputerModule.COMPUTER_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
