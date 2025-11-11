package dev.gacbl.logicore.compat.jade;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.cpucore.CPUCoreBlock;
import dev.gacbl.logicore.cpucore.CPUCoreBlockEntity;
import dev.gacbl.logicore.serverrack.ServerRackBlock;
import dev.gacbl.logicore.serverrack.ServerRackBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;


@WailaPlugin(value = LogiCore.MOD_ID)
public class LogiCoreJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CPUCoreProvider.INSTANCE, CPUCoreBlock.class);
        registration.registerBlockComponent(ServerRackProvider.INSTANCE, ServerRackBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CPUCoreProvider.INSTANCE, CPUCoreBlockEntity.class);
        registration.registerBlockDataProvider(ServerRackProvider.INSTANCE, ServerRackBlockEntity.class);
    }
}
