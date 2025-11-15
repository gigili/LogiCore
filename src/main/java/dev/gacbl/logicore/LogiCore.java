package dev.gacbl.logicore;

import com.mojang.logging.LogUtils;
import dev.gacbl.logicore.computer.ComputerModule;
import dev.gacbl.logicore.core.CoreModule;
import dev.gacbl.logicore.core.CreativeTabModule;
import dev.gacbl.logicore.datacable.DataCableModule;
import dev.gacbl.logicore.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.serverrack.ServerRackDataComponent;
import dev.gacbl.logicore.serverrack.ServerRackModule;
import dev.gacbl.logicore.serverrack.ui.ServerRackScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(LogiCore.MOD_ID)
public class LogiCore {
    public static final String MOD_ID = "logicore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LogiCore(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        CreativeTabModule.register(modEventBus);

        CoreModule.register(modEventBus);

        ProcessorUnitModule.register(modEventBus);

        ComputerModule.register(modEventBus);

        ServerRackModule.register(modEventBus);
        ServerRackDataComponent.register(modEventBus);

        DataCableModule.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "logicore.toml");
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ServerRackModule.SERVER_RACK_MENU.get(), ServerRackScreen::new);
        }
    }
}
