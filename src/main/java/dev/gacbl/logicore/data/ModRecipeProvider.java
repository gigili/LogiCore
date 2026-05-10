package dev.gacbl.logicore.data;

import dev.gacbl.logicore.blocks.battery.BatteryBlock;
import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.battery.BatteryTier;
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
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.server.ServerItem;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeItem;
import dev.gacbl.logicore.items.wrench.WrenchItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static Runner createRunner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        return new Runner(packOutput, registries);
    }

    public static class Runner extends RecipeProvider.Runner {
        protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "LogiCore Recipes";
        }
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.items;
        ServerRackBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        DataCableBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        ComputerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        DatacenterControllerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        CompilerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        DatacenterPortBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        GeneratorBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        CloudInterfaceBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        WrenchItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        StackUpgradeItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        ResearchStationBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        RepairStationBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        CyclePickItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        RecyclerBlock.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);
        ServerItem.getRecipe(items).unlockedBy("has_processor", has(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get())).save(this.output);

        List<Item> batteryItems = BatteryModule.ITEMS.getEntries().stream()
                .map(Holder::value)
                .toList();

        List<Item> processorItems = ProcessorUnitModule.ITEMS.getEntries().stream()
                .map(Holder::value)
                .toList();

        AtomicInteger index = new AtomicInteger(0);

        ProcessorUnitModule.ITEMS.getEntries().forEach(itemHolder -> {
            int i = index.getAndIncrement();
            ItemLike coreIngredient = (i == 0) ? Items.GOLD_INGOT : processorItems.get(i - 1);
            Item other = (i == 0) ? Items.IRON_INGOT : (i == 1) ? Items.EMERALD : Items.NETHER_STAR;
            TagKey<Item> redstone = (i == 0) ? ItemTags.create(Identifier.fromNamespaceAndPath("c", "dusts/redstone")) : ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/redstone"));

            ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, itemHolder.get())
                    .pattern("QRQ")
                    .pattern("IGI")
                    .pattern("QRQ")
                    .define('I', other)
                    .define('G', coreIngredient)
                    .define('R', redstone)
                    .define('Q', ItemTags.create(Identifier.fromNamespaceAndPath("c", "gems/quartz")))
                    .unlockedBy("has_core", has(coreIngredient))
                    .save(this.output);
        });

        index.set(0);

        BatteryModule.BLOCKS.getEntries().forEach(blockHolder -> {
            if (blockHolder.get() instanceof BatteryBlock batteryBlock && batteryBlock.getTier() == BatteryTier.CREATIVE)
                return;
            int i = index.getAndIncrement();
            ItemLike coreIngredient = (i == 0) ? ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get() : batteryItems.get(i - 1);
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
                    .save(this.output);
        });
    }
}
