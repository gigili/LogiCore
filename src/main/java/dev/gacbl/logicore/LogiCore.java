package dev.gacbl.logicore;

import com.mojang.logging.LogUtils;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.repair_station.RepairStationModule;
import dev.gacbl.logicore.blocks.research_station.ResearchStationModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.core.IntegrationUtils;
import dev.gacbl.logicore.core.ModDataMaps;
import dev.gacbl.logicore.core.MyCommands;
import dev.gacbl.logicore.data.CreativeTabModule;
import dev.gacbl.logicore.items.pickaxe.CyclePickModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncAllPlayerKnowledgePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod(LogiCore.MOD_ID)
public class LogiCore {
    public static final String MOD_ID = "logicore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final UUID LASH_UUID = UUID.fromString("1ede36f5-cc12-47c1-bb01-d6c445d01a17");
    public static final UUID DIRE_UUID = UUID.fromString("bbb87dbe-690f-4205-bdc5-72ffb8ebc29d");
    public static final UUID GAC_UUID = UUID.fromString("73790c93-7c66-4147-9156-a12dcbd95c02");
    public static final UUID DEV_UUID = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");

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
        //DroneBayModule.register(modEventBus);
        GeneratorModule.register(modEventBus);
        CloudInterfaceModule.register(modEventBus);
        WrenchModule.register(modEventBus);
        //DroneModule.register(modEventBus);
        BatteryModule.register(modEventBus);
        StackUpgradeModule.register(modEventBus);
        ResearchStationModule.register(modEventBus);
        CyclePickModule.register(modEventBus);
        RepairStationModule.register(modEventBus);

        IntegrationUtils.registerEvents();
        PacketHandler.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "logicore/logicore.toml");
        modEventBus.addListener(this::registerDataMaps);

        //Guide.builder(ResourceLocation.parse("logicore:guide")).build();
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

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        CycleValueManager.reload(
                event.getServer().getRecipeManager(),
                event.getServer().registryAccess(),
                true
        );

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            LogiCore.syncCycleSavedDataToPlayer(player, player.serverLevel());
        }
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) { // Null player means "reload all"
            CycleValueManager.reload(
                    event.getPlayerList().getServer().getRecipeManager(),
                    event.getPlayerList().getServer().registryAccess(),
                    true
            );

            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                LogiCore.syncCycleSavedDataToPlayer(player, player.serverLevel());
            }
        } else {
            if (event.getPlayer() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
                LogiCore.syncCycleSavedDataToPlayer(player, level);
            }
        }
    }

    public static void syncCycleSavedDataToPlayer(ServerPlayer player, ServerLevel level) {
        CycleSavedData data = CycleSavedData.get(level);
        String playerKey = CycleSavedData.getKey(level, player.getUUID());

        Set<String> unlockedItems = data.getKnowledge(playerKey);

        if (unlockedItems != null) {
            List<String> keys = new ArrayList<>(unlockedItems);
            PacketHandler.sendToPlayer(player, new SyncAllPlayerKnowledgePayload(keys));
        }
    }

    //@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        /*@SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            //EntityRenderers.register(DroneModule.DRONE.get(), DroneRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            //event.registerLayerDefinition(DroneModel.LAYER_LOCATION, DroneModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            //event.put(DroneModule.DRONE.get(), DroneEntity.createAttributes().build());
        }*/
    }
}
