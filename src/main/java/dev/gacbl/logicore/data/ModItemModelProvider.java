package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayModule;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LogiCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ProcessorUnitModule.PROCESSOR_UNIT.get());

        withExistingParent(CompilerModule.COMPILER_ITEM.getId().getPath(), "logicore:block/" + CompilerModule.COMPILER_ITEM.getId().getPath());
        withExistingParent(ComputerModule.COMPUTER_ITEM.getId().getPath(), "logicore:block/" + ComputerModule.COMPUTER_ITEM.getId().getPath());
        withExistingParent(DataCableModule.DATA_CABLE_ITEM.getId().getPath(), "logicore:block/data_cable_core");
        withExistingParent(DatacenterModule.DATACENTER_CONTROLLER_ITEM.getId().getPath(), "logicore:block/" + DatacenterModule.DATACENTER_CONTROLLER_ITEM.getId().getPath());
        withExistingParent(DatacenterPortModule.DATACENTER_PORT_ITEM.getId().getPath(), "logicore:block/" + DatacenterPortModule.DATACENTER_PORT_ITEM.getId().getPath());
        withExistingParent(DroneBayModule.DRONE_BAY_ITEM.getId().getPath(), "logicore:block/" + DroneBayModule.DRONE_BAY_ITEM.getId().getPath());
        withExistingParent(ServerRackModule.SERVER_RACK_ITEM.getId().getPath(), "logicore:block/" + ServerRackModule.SERVER_RACK_ITEM.getId().getPath());
        withExistingParent(GeneratorModule.GENERATOR_ITEM.getId().getPath(), "logicore:block/" + GeneratorModule.GENERATOR_ITEM.getId().getPath());
    }
}
