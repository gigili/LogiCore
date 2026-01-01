package dev.gacbl.logicore.data;

import dev.gacbl.logicore.blocks.battery.advance.AdvanceBatteryBlock;
import dev.gacbl.logicore.blocks.battery.basic.BasicBatteryBlock;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceBlock;
import dev.gacbl.logicore.blocks.compiler.CompilerBlock;
import dev.gacbl.logicore.blocks.computer.ComputerBlock;
import dev.gacbl.logicore.blocks.datacable.DataCableBlock;
import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlock;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortBlock;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayBlock;
import dev.gacbl.logicore.blocks.generator.GeneratorBlock;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlock;
import dev.gacbl.logicore.entity.drone.DroneItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.wrench.WrenchItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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
        DroneBayBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        DroneItem.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        GeneratorBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        CloudInterfaceBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        WrenchItem.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        BasicBatteryBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
        AdvanceBatteryBlock.getRecipe().unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT.get())).save(recipeOutput);
    }
}
