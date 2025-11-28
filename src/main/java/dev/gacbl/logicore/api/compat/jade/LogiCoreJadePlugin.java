package dev.gacbl.logicore.api.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlockEntity;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlock;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
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
        registration.registerBlockComponent(DatacenterProvider.INSTANCE, DatacenterControllerBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CoreCycleProvider.INSTANCE, ServerRackBlockEntity.class);
        registration.registerBlockDataProvider(CoreCycleProvider.INSTANCE, ComputerBlockEntity.class);
        registration.registerBlockDataProvider(CompilerProvider.INSTANCE, CompilerBlockEntity.class);
        registration.registerBlockDataProvider(DatacenterProvider.INSTANCE, DatacenterControllerBlockEntity.class);
    }
}
