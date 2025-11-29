package dev.gacbl.logicore;

import com.mojang.logging.LogUtils;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerScreen;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackScreen;
import dev.gacbl.logicore.core.CreativeTabModule;
import dev.gacbl.logicore.core.ModDataMaps;
import dev.gacbl.logicore.core.MyCommands;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.network.PacketHandler;
import guideme.Guide;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;

@Mod(LogiCore.MOD_ID)
public class LogiCore {
    public static final String MOD_ID = "logicore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LogiCore(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        CreativeTabModule.register(modEventBus);
        ProcessorUnitModule.register(modEventBus);
        ComputerModule.register(modEventBus);
        ServerRackModule.register(modEventBus);
        DataCableModule.register(modEventBus);
        DatacenterModule.register(modEventBus);
        DatacenterPortModule.register(modEventBus);
        CompilerModule.register(modEventBus);
        PacketHandler.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "logicore/logicore.toml");
        modEventBus.addListener(this::registerDataMaps);

        Guide.builder(ResourceLocation.parse("logicore:guide")).build();
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(ModDataMaps.ITEM_CYCLES);
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            NetworkManager.get(serverLevel).tick(serverLevel);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MyCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
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
            event.register(CompilerModule.COMPILER_MENU.get(), CompilerScreen::new);
        }
    }
}
