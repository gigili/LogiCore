package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.battery.advance.AdvanceBatteryLootTableProvider;
import dev.gacbl.logicore.blocks.battery.basic.BasicBatteryLootTableProvider;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceLootTableProvider;
import dev.gacbl.logicore.blocks.compiler.CompilerLootTableProvider;
import dev.gacbl.logicore.blocks.computer.ComputerLootTableProvider;
import dev.gacbl.logicore.blocks.datacable.DataCableLootTableProvider;
import dev.gacbl.logicore.blocks.datacenter.DataCenterControllerLootTableProvider;
import dev.gacbl.logicore.blocks.datacenter.DatacenterPortLootTableProvider;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayLootTableProvider;
import dev.gacbl.logicore.blocks.generator.GeneratorLootTableProvider;
import dev.gacbl.logicore.blocks.research_station.ResearchStationLootTableProvider;
import dev.gacbl.logicore.blocks.serverrack.ServerRackLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = LogiCore.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        packOutput, Collections.emptySet(),
                        List.of(
                                new LootTableProvider.SubProviderEntry(ServerRackLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(DataCableLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ComputerLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(DataCenterControllerLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(DatacenterPortLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(CompilerLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(DroneBayLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(CloudInterfaceLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(GeneratorLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(BasicBatteryLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(AdvanceBatteryLootTableProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ResearchStationLootTableProvider::new, LootContextParamSets.BLOCK)
                        ),
                        lookupProvider
                )
        );

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(packOutput, lookupProvider));

        BlockTagsProvider blockTagsProvider = new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        //generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
    }
}
