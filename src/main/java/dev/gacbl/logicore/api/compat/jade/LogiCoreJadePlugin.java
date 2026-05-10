package dev.gacbl.logicore.api.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.compat.jade.providers.*;
import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlockEntity;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlock;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlock;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlock;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlockEntity;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlock;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntity;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;


@WailaPlugin(value = LogiCore.MOD_ID)
public class LogiCoreJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CoreCycleProvider.INSTANCE, ServerRackBlock.class);
        registration.registerBlockComponent(CoreCycleProvider.INSTANCE, ComputerBlock.class);
        registration.registerBlockComponent(CompilerProvider.INSTANCE, CompilerBlock.class);
        registration.registerBlockComponent(DatacenterProvider.INSTANCE, DatacenterPortBlock.class);
        registration.registerBlockComponent(ResearchStationProvider.INSTANCE, ResearchStationBlock.class);
        registration.registerBlockComponent(RepairStationProvider.INSTANCE, RepairStationBlock.class);
        registration.registerBlockComponent(RecyclerProvider.INSTANCE, RecyclerBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CoreCycleDataProvider.INSTANCE, ServerRackBlockEntity.class);
        registration.registerBlockDataProvider(CoreCycleDataProvider.INSTANCE, ComputerBlockEntity.class);
        registration.registerBlockDataProvider(CompilerDataProvider.INSTANCE, CompilerBlockEntity.class);
        registration.registerBlockDataProvider(DatacenterDataProvider.INSTANCE, DatacenterPortBlockEntity.class);
        registration.registerBlockDataProvider(ResearchStationDataProvider.INSTANCE, ResearchStationBlockEntity.class);
        registration.registerBlockDataProvider(RepairStationDataProvider.INSTANCE, RepairStationBlockEntity.class);
        registration.registerBlockDataProvider(RecyclerDataProvider.INSTANCE, RecyclerBlockEntity.class);
    }
}
