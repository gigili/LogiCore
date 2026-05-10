package dev.gacbl.logicore.api.compat.ae2;

// ============================================================
// AE2 Helper - DISABLED during 26.1 port
// To re-enable: Remove the /* and */ comment markers below,
// ensure AE2 (Applied Energistics 2) is available on the
// classpath with the correct API for Minecraft 26.1.
// ============================================================
/*
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
*/
