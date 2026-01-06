package dev.gacbl.logicore.api.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.compat.jade.providers.*;
import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlockEntity;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlock;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlockEntity;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlock;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlockEntity;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlock;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntity;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import dev.gacbl.logicore.entity.drone.DroneEntity;
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
        registration.registerBlockComponent(DroneBayProvider.INSTANCE, DroneBayBlock.class);
        registration.registerBlockComponent(ResearchStationProvider.INSTANCE, ResearchStationBlock.class);

        registration.registerEntityComponent(DroneEntityProvider.INSTANCE, DroneEntity.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CoreCycleProvider.INSTANCE, ServerRackBlockEntity.class);
        registration.registerBlockDataProvider(CoreCycleProvider.INSTANCE, ComputerBlockEntity.class);
        registration.registerBlockDataProvider(CompilerProvider.INSTANCE, CompilerBlockEntity.class);
        registration.registerBlockDataProvider(DatacenterProvider.INSTANCE, DatacenterPortBlockEntity.class);
        registration.registerBlockDataProvider(DroneBayProvider.INSTANCE, DroneBayBlockEntity.class);
        registration.registerBlockDataProvider(ResearchStationProvider.INSTANCE, ResearchStationBlockEntity.class);

        registration.registerEntityDataProvider(DroneEntityProvider.INSTANCE, DroneEntity.class);
    }
}
