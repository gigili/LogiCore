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
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, LogiCore.MOD_ID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_ADVANCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_ULTIMATE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(WrenchModule.WRENCH.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(StackUpgradeModule.STACK_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(CyclePickModule.CYCLE_PICK.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(KnowledgeOrbModule.KNOWLEDGE_ORB.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ServerModule.SERVER.get(), ModelTemplates.FLAT_ITEM);

        blockModels.createFlatItemModelWithBlockTexture(CompilerModule.COMPILER_ITEM.get(), CompilerModule.COMPILER_BLOCK.get());
        blockModels.createFlatItemModelWithBlockTexture(ComputerModule.COMPUTER_ITEM.get(), ComputerModule.COMPUTER_BLOCK.get());
        blockModels.createFlatItemModelWithBlockTexture(DataCableModule.DATA_CABLE_ITEM.get(), DataCableModule.DATA_CABLE_BLOCK.get());
        blockModels.createFlatItemModelWithBlockTexture(DatacenterModule.DATACENTER_CONTROLLER_ITEM.get(), DatacenterModule.DATACENTER_CONTROLLER.get());
        blockModels.createFlatItemModelWithBlockTexture(DatacenterPortModule.DATACENTER_PORT_ITEM.get(), DatacenterPortModule.DATACENTER_PORT.get());
        blockModels.createFlatItemModelWithBlockTexture(GeneratorModule.GENERATOR_ITEM.get(), GeneratorModule.GENERATOR.get());
        blockModels.createFlatItemModelWithBlockTexture(CloudInterfaceModule.CLOUD_INTERFACE_ITEM.get(), CloudInterfaceModule.CLOUD_INTERFACE.get());
        blockModels.createFlatItemModelWithBlockTexture(ResearchStationModule.RESEARCH_STATION_ITEM.get(), ResearchStationModule.RESEARCH_STATION.get());
        blockModels.createFlatItemModelWithBlockTexture(RepairStationModule.REPAIR_STATION_ITEM.get(), RepairStationModule.REPAIR_STATION.get());
        blockModels.createFlatItemModelWithBlockTexture(RecyclerModule.RECYCLER_ITEM.get(), RecyclerModule.RECYCLER.get());
        blockModels.createFlatItemModelWithBlockTexture(ServerRackModule.SERVER_RACK_ITEM.get(), ServerRackModule.SERVER_RACK.get());
        blockModels.createFlatItemModelWithBlockTexture(BatteryModule.BATTERY_CREATIVE.asItem(), BatteryModule.BATTERY_CREATIVE.get());
        blockModels.createFlatItemModelWithBlockTexture(BatteryModule.BATTERY_LARGE.asItem(), BatteryModule.BATTERY_LARGE.get());
        blockModels.createFlatItemModelWithBlockTexture(BatteryModule.BATTERY_MEDIUM.asItem(), BatteryModule.BATTERY_MEDIUM.get());
        blockModels.createFlatItemModelWithBlockTexture(BatteryModule.BATTERY_SMALL.asItem(), BatteryModule.BATTERY_SMALL.get());
    }
}
