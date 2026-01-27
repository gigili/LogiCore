package dev.gacbl.logicore.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.battery.BatteryFillRenderer;
import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntityRenderer;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerScreen;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.generator.ui.GeneratorScreen;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlockEntityRenderer;
import dev.gacbl.logicore.blocks.repair_station.RepairStationModule;
import dev.gacbl.logicore.blocks.repair_station.ui.RepairStationScreen;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntityRenderer;
import dev.gacbl.logicore.blocks.research_station.ResearchStationModule;
import dev.gacbl.logicore.blocks.research_station.ui.ResearchStationScreen;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.blocks.serverrack.ui.ServerRackScreen;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = LogiCore.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "cycle_hud"), (gui, deltaTracker) -> {
            long cycles = ClientCycleData.getCycles();

            Player player = Minecraft.getInstance().player;
            if (player != null && player.isShiftKeyDown()) {
                gui.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("tooltip.logicore.ui.cloud_cycles", cycles),
                        10, 10, 0xFFFFFF
                );
            } else {
                gui.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("tooltip.logicore.ui.cloud_cycles", Utils.formatValues(cycles)),
                        10, 10, 0xFFFFFF
                );
            }
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ServerRackModule.SERVER_RACK_MENU.get(), ServerRackScreen::new);
        event.register(CompilerModule.COMPILER_MENU.get(), CompilerScreen::new);
        event.register(GeneratorModule.GENERATOR_MENU.get(), GeneratorScreen::new);
        event.register(ResearchStationModule.RESEARCH_STATION_MENU.get(), ResearchStationScreen::new);
        event.register(RepairStationModule.REPAIR_STATION_MENU.get(), RepairStationScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //event.registerBlockEntityRenderer(DroneBayModule.DRONE_BAY_BE.get(), DroneBayRenderer::new);
        event.registerBlockEntityRenderer(CompilerModule.COMPILER_BLOCK_ENTITY.get(), CompilerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ResearchStationModule.RESEARCH_STATION_BE.get(), ResearchStationBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BatteryModule.BATTERY_BE.get(), BatteryFillRenderer::new);
        event.registerBlockEntityRenderer(RepairStationModule.REPAIR_STATION_BE.get(), RepairStationBlockEntityRenderer::new);
    }
}
