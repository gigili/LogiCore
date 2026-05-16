package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.battery.BatteryModule;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.recycler.RecyclerModule;
import dev.gacbl.logicore.blocks.repair_station.RepairStationModule;
import dev.gacbl.logicore.blocks.research_station.ResearchStationModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.items.knowledge_orb.KnowledgeOrbModule;
import dev.gacbl.logicore.items.pickaxe.CyclePickModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.stack_upgrade.StackUpgradeModule;
import dev.gacbl.logicore.items.wrench.WrenchModule;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, LogiCore.MOD_ID);
    }

    @Override
    protected @NonNull Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected @NonNull Stream<? extends Holder<Item>> getKnownItems() {
        List<Holder<Item>> list = new ArrayList<>();

        list.addAll(ProcessorUnitModule.ITEMS.getEntries().stream().toList());
        list.addAll(WrenchModule.ITEMS.getEntries().stream().toList());
        list.addAll(StackUpgradeModule.ITEMS.getEntries().stream().toList());
        list.addAll(CyclePickModule.ITEMS.getEntries().stream().toList());
        list.addAll(KnowledgeOrbModule.ITEMS.getEntries().stream().toList());

        list.addAll(BatteryModule.ITEMS.getEntries().stream().toList());
        list.addAll(CloudInterfaceModule.ITEMS.getEntries().stream().toList());
        list.addAll(CompilerModule.ITEMS.getEntries().stream().toList());
        list.addAll(ComputerModule.ITEMS.getEntries().stream().toList());
        list.addAll(DataCableModule.ITEMS.getEntries().stream().toList());
        list.addAll(DatacenterModule.ITEMS.getEntries().stream().toList());
        list.addAll(DatacenterPortModule.ITEMS.getEntries().stream().toList());
        list.addAll(GeneratorModule.ITEMS.getEntries().stream().toList());
        list.addAll(RecyclerModule.ITEMS.getEntries().stream().toList());
        list.addAll(RepairStationModule.ITEMS.getEntries().stream().toList());
        list.addAll(ResearchStationModule.ITEMS.getEntries().stream().toList());
        list.addAll(ServerRackModule.ITEMS.getEntries().stream().toList());

        return list.stream();
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_ADVANCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ProcessorUnitModule.PROCESSOR_UNIT_ULTIMATE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(WrenchModule.WRENCH.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(StackUpgradeModule.STACK_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(CyclePickModule.CYCLE_PICK.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(KnowledgeOrbModule.KNOWLEDGE_ORB.get(), ModelTemplates.FLAT_ITEM);
    }
}
