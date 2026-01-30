package dev.gacbl.logicore.data;

import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceBlock;
import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlock;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlock;
import dev.gacbl.logicore.blocks.generator.GeneratorBlock;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlock;
import dev.gacbl.logicore.blocks.repair_station.RepairStationBlock;
import dev.gacbl.logicore.blocks.research_station.ResearchStationBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.items.pickaxe.CyclePickItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import dev.gacbl.logicore.items.wrench.WrenchItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ProcessorUnitItem.getRecipe().unlockedBy("has_redstone", has(Items.REDSTONE)).save(recipeOutput);
        ServerRackBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        DataCableBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        ComputerBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        DatacenterControllerBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        CompilerBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        DatacenterPortBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        //DroneBayBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        //DroneItem.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        GeneratorBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        CloudInterfaceBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        WrenchItem.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        StackUpgradeItem.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        ResearchStationBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        RepairStationBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        CyclePickItem.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        RecyclerBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);

        List<Item> batteryItems = BatteryModule.ITEMS.getEntries().stream()
                .map(Holder::value)
                .toList();

        AtomicInteger index = new AtomicInteger(0);

        BatteryModule.BLOCKS.getEntries().forEach(blockHolder -> {
            int i = index.getAndIncrement();
            ItemLike coreIngredient = (i == 0) ? ProcessorUnitModule.PROCESSOR_UNIT.get() : batteryItems.get(i - 1);
            Item gold = (i == 0) ? Items.GOLD_INGOT : (i == 2) ? Items.DIAMOND_BLOCK : Items.GOLD_BLOCK;
            Item redstone = (i == 0) ? Items.REDSTONE : Items.REDSTONE_BLOCK;

            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, blockHolder.get())
                    .pattern("RGR")
                    .pattern("GPG")
                    .pattern("RGR")
                    .define('G', gold)
                    .define('R', redstone)
                    .define('P', coreIngredient)
                    .unlockedBy("has_core", has(coreIngredient))
                    .save(recipeOutput);
        });
    }
}
