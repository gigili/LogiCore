package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.recycler.RecyclerModule;
import dev.gacbl.logicore.blocks.repair_station.RepairStationModule;
import dev.gacbl.logicore.blocks.research_station.ResearchStationModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.items.knowledge_orb.KnowledgeOrbModule;
import dev.gacbl.logicore.items.pickaxe.CyclePickModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.server.ServerModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabModule {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LogiCore.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOGICORE_TAB =
            CREATIVE_MODE_TABS.register("logicore_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.logicore.tab"))
                    .icon(() -> new ItemStack(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(ServerRackModule.SERVER_RACK.get()));
                        output.accept(new ItemStack(DataCableModule.DATA_CABLE_ITEM.get()));
                        output.accept(new ItemStack(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get()));
                        output.accept(new ItemStack(ProcessorUnitModule.PROCESSOR_UNIT_ADVANCE.get()));
                        output.accept(new ItemStack(ProcessorUnitModule.PROCESSOR_UNIT_ULTIMATE.get()));
                        output.accept(new ItemStack(ComputerModule.COMPUTER_ITEM.get()));
                        output.accept(new ItemStack(DatacenterModule.DATACENTER_CONTROLLER.get()));
                        output.accept(new ItemStack(CompilerModule.COMPILER_ITEM.get()));
                        output.accept(new ItemStack(DatacenterPortModule.DATACENTER_PORT.get()));
                        output.accept(new ItemStack(GeneratorModule.GENERATOR.get()));
                        output.accept(new ItemStack(CloudInterfaceModule.CLOUD_INTERFACE.get()));
                        output.accept(new ItemStack(WrenchModule.WRENCH.get()));
                        output.accept(new ItemStack(BatteryModule.BATTERY_SMALL.get()));
                        output.accept(new ItemStack(BatteryModule.BATTERY_MEDIUM.get()));
                        output.accept(new ItemStack(BatteryModule.BATTERY_LARGE.get()));
                        output.accept(new ItemStack(BatteryModule.BATTERY_CREATIVE.get()));
                        output.accept(new ItemStack(StackUpgradeModule.STACK_UPGRADE.get()));
                        output.accept(new ItemStack(ResearchStationModule.RESEARCH_STATION.get()));
                        output.accept(new ItemStack(CyclePickModule.CYCLE_PICK.get()));
                        output.accept(new ItemStack(RepairStationModule.REPAIR_STATION.get()));
                        output.accept(new ItemStack(RecyclerModule.RECYCLER.get()));
                        output.accept(new ItemStack(ServerModule.SERVER.get()));
                        output.accept(new ItemStack(KnowledgeOrbModule.KNOWLEDGE_ORB.get()));
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
