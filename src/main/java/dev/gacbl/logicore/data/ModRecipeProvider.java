package dev.gacbl.logicore.data;

import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceBlock;
import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlock;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlock;
import dev.gacbl.logicore.blocks.generator.GeneratorBlock;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlock;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.items.pickaxe.CyclePickItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import dev.gacbl.logicore.items.wrench.WrenchItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);

        ProcessorUnitItem.getRecipe(items).unlockedBy("has_redstone", has(Items.REDSTONE)).save(output);
        ServerRackBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        DataCableBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        ComputerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        DatacenterControllerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        CompilerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        DatacenterPortBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        GeneratorBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        CloudInterfaceBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        WrenchItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        StackUpgradeItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        ResearchStationBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        RepairStationBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);
        CyclePickItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(output);

        List<Item> batteryItems = BatteryModule.ITEMS.getEntries().stream()
                .map(Holder::value)
                .toList();

        AtomicInteger index = new AtomicInteger(0);

        BatteryModule.BLOCKS.getEntries().forEach(blockHolder -> {
            int i = index.getAndIncrement();
            ItemLike coreIngredient = (i == 0) ? ProcessorUnitModule.PROCESSOR_UNIT.get() : batteryItems.get(i - 1);
            Item gold = (i == 0) ? Items.GOLD_INGOT : (i == 2) ? Items.DIAMOND_BLOCK : Items.GOLD_BLOCK;
            Item redstone = (i == 0) ? Items.REDSTONE : Items.REDSTONE_BLOCK;

            ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, blockHolder.get())
                    .pattern("RGR")
                    .pattern("GPG")
                    .pattern("RGR")
                    .define('G', gold)
                    .define('R', redstone)
                    .define('P', coreIngredient)
                    .unlockedBy("has_core", has(coreIngredient))
                    .save(output);
        });
    }

    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registries, @NotNull RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "My Recipes";
        }
    }
}
