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
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

public class ModItemModelProvider extends ModelProvider {

    public ModItemModelProvider(PackOutput output) {
        super(output, LogiCore.MOD_ID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_ADVANCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_ULTIMATE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(WrenchModule.WRENCH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(StackUpgradeModule.STACK_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(CyclePickModule.CYCLE_PICK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(KnowledgeOrbModule.KNOWLEDGE_ORB.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(CompilerModule.COMPILER_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ComputerModule.COMPUTER_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DataCableModule.DATA_CABLE_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DatacenterModule.DATACENTER_CONTROLLER_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DatacenterPortModule.DATACENTER_PORT_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ServerRackModule.SERVER_RACK_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GeneratorModule.GENERATOR_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(CloudInterfaceModule.CLOUD_INTERFACE_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ResearchStationModule.RESEARCH_STATION_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RepairStationModule.REPAIR_STATION_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RecyclerModule.RECYCLER_ITEM.get(), ModelTemplates.FLAT_ITEM);

        BatteryModule.ITEMS.getEntries().forEach(holder -> {
            itemModels.generateFlatItem(holder.get(), ModelTemplates.FLAT_ITEM);
        });
    }
}
