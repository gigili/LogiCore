package dev.gacbl.logicore.api.compat.ae2;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceBlockEntity;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class Ae2Helper {
    public static IGridNodeService createService(CloudInterfaceBlockEntity be) {
        return new CloudAe2Service(be);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                CloudInterfaceModule.CLOUD_INTERFACE_BE.get(),
                (be, context) -> {
                    IGridNodeService service = be.getAe2Service();
                    if (service instanceof IInWorldGridNodeHost host) {
                        return host;
                    }
                    return null;
                }
        );
    }
}
