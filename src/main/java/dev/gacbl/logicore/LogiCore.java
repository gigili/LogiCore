package dev.gacbl.logicore;

import com.mojang.logging.LogUtils;
import dev.gacbl.logicore.blocks.battery.BatteryFillRenderer;
import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntityRenderer;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerScreen;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayModule;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayRenderer;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.generator.ui.GeneratorScreen;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntityRenderer;
import dev.gacbl.logicore.blocks.research_station.ResearchStationModule;
import dev.gacbl.logicore.blocks.research_station.ui.ResearchStationScreen;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackScreen;
import dev.gacbl.logicore.core.IntegrationUtils;
import dev.gacbl.logicore.core.ModDataMaps;
import dev.gacbl.logicore.core.MyCommands;
import dev.gacbl.logicore.data.CreativeTabModule;
import dev.gacbl.logicore.entity.drone.DroneEntity;
import dev.gacbl.logicore.entity.drone.DroneModule;
import dev.gacbl.logicore.entity.drone.client.DroneModel;
import dev.gacbl.logicore.entity.drone.client.DroneRenderer;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import dev.gacbl.logicore.network.PacketHandler;
import guideme.Guide;
import net.minecraft.client.renderer.entity.EntityRenderers;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;

import java.util.UUID;

@Mod(LogiCore.MOD_ID)
public class LogiCore {
    public static final String MOD_ID = "logicore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final UUID LASH_UUID = UUID.fromString("1ede36f5-cc12-47c1-bb01-d6c445d01a17");
    public static final UUID DIRE_UUID = UUID.fromString("bbb87dbe-690f-4205-bdc5-72ffb8ebc29d");
    public static final UUID GAC_UUID = UUID.fromString("73790c93-7c66-4147-9156-a12dcbd95c02");

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
        DroneBayModule.register(modEventBus);
        GeneratorModule.register(modEventBus);
        CloudInterfaceModule.register(modEventBus);
        WrenchModule.register(modEventBus);
        DroneModule.register(modEventBus);
        BatteryModule.register(modEventBus);
        StackUpgradeModule.register(modEventBus);
        ResearchStationModule.register(modEventBus);

        IntegrationUtils.registerEvents();
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
            EntityRenderers.register(DroneModule.DRONE.get(), DroneRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ServerRackModule.SERVER_RACK_MENU.get(), ServerRackScreen::new);
            event.register(CompilerModule.COMPILER_MENU.get(), CompilerScreen::new);
            event.register(GeneratorModule.GENERATOR_MENU.get(), GeneratorScreen::new);
            event.register(ResearchStationModule.RESEARCH_STATION_MENU.get(), ResearchStationScreen::new);
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(CompilerModule.COMPILER_BLOCK_ENTITY.get(), CompilerBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ResearchStationModule.RESEARCH_STATION_BE.get(), ResearchStationBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(DroneBayModule.DRONE_BAY_BE.get(), DroneBayRenderer::new);
            event.registerBlockEntityRenderer(BatteryModule.BATTERY_BE.get(), BatteryFillRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(DroneModel.LAYER_LOCATION, DroneModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(DroneModule.DRONE.get(), DroneEntity.createAttributes().build());
        }
    }
}
