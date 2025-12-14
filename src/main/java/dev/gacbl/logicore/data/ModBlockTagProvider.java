package dev.gacbl.logicore.data;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceModule;
import dev.gacbl.logicore.blocks.compiler.CompilerModule;
import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.blocks.datacenter_port.DatacenterPortModule;
import dev.gacbl.logicore.blocks.drone_bay.DroneBayModule;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.core.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, LogiCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported"))).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("mekanism", "cardboard_blacklist"))).add(ServerRackModule.SERVER_RACK_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported"))).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("mekanism", "cardboard_blacklist"))).add(DataCableModule.DATA_CABLE_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported"))).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("mekanism", "cardboard_blacklist"))).add(ComputerModule.COMPUTER_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported"))).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("mekanism", "cardboard_blacklist"))).add(DatacenterModule.DATACENTER_CONTROLLER.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(CompilerModule.COMPILER_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(CompilerModule.COMPILER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(CompilerModule.COMPILER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(CompilerModule.COMPILER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(CompilerModule.COMPILER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported"))).add(CompilerModule.COMPILER_BLOCK.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("mekanism", "cardboard_blacklist"))).add(CompilerModule.COMPILER_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported"))).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("mekanism", "cardboard_blacklist"))).add(DatacenterPortModule.DATACENTER_PORT.get());
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).add(DatacenterPortModule.DATACENTER_PORT.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(DroneBayModule.DRONE_BAY.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(DroneBayModule.DRONE_BAY.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(DroneBayModule.DRONE_BAY.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(DroneBayModule.DRONE_BAY.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(DroneBayModule.DRONE_BAY.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(GeneratorModule.GENERATOR.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(GeneratorModule.GENERATOR.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(GeneratorModule.GENERATOR.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(GeneratorModule.GENERATOR.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(GeneratorModule.GENERATOR.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(CloudInterfaceModule.CLOUD_INTERFACE.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(CloudInterfaceModule.CLOUD_INTERFACE.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "eclipsegate_deny"))).add(CloudInterfaceModule.CLOUD_INTERFACE.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "phase_deny"))).add(CloudInterfaceModule.CLOUD_INTERFACE.get());
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("justdirethings", "swapper_deny"))).add(CloudInterfaceModule.CLOUD_INTERFACE.get());

        setupOptionalTags();
    }

    private void setupOptionalTags() {
        tag(ModTags.Blocks.IS_ENERGY_GENERATOR).addOptional(ResourceLocation.fromNamespaceAndPath("justdirethings", "generatort1"));
        tag(ModTags.Blocks.IS_ENERGY_GENERATOR).addOptional(ResourceLocation.fromNamespaceAndPath("justdirethings", "generatortfluid1"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "quantum_entagloporter"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("flux_networks", "flux_point"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "basic_universal_cable"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "advanced_universal_cable"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "elite_universal_cable"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_universal_cable"));
        tag(ModTags.Blocks.IS_ENERGY_CABLE).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("powah", "energy_cables")));

        //Outer frame blocks
        tag(ModTags.Blocks.VALID_DATACENTER_FRAME_BLOCK).add(Blocks.IRON_BLOCK);
        tag(ModTags.Blocks.VALID_DATACENTER_FRAME_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "concretes")));
        tag(ModTags.Blocks.VALID_DATACENTER_FRAME_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "obsidians")));

        //Wall blocks
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).add(Blocks.IRON_BLOCK);
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).add(Blocks.IRON_DOOR);
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).add(DatacenterModule.DATACENTER_CONTROLLER.get());
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "concretes")));
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "stones")));
        tag(ModTags.Blocks.VALID_DATACENTER_WALL_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "glass_blocks")));

        //Inner blocks
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(ServerRackModule.SERVER_RACK_BLOCK.get());
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(ComputerModule.COMPUTER_BLOCK.get());
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(DataCableModule.DATA_CABLE_BLOCK.get());
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.TORCH);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.LIGHT);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.GLOW_LICHEN);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.REDSTONE_BLOCK);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.GLOWSTONE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.SHROOMLIGHT);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.EXPOSED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.OXIDIZED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.WAXED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.WEATHERED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.WAXED_EXPOSED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.WAXED_OXIDIZED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).add(Blocks.WAXED_WEATHERED_COPPER_GRATE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(ModTags.Blocks.IS_ENERGY_CABLE);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(ModTags.Blocks.IS_ENERGY_GENERATOR);
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "pressure_plates")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "buttons")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "stairs")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "fences")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "fence_gates")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("powah", "reactors")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("powah", "energy_cable")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("powah", "ender_gates")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("powah", "ender_cells")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("powah", "energy_cells")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("ae2", "controller")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("ae2", "glass_cable")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("ae2", "smart_cable")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("ae2", "covered_cable")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("ae2", "smart_dense_cable")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptionalTag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("ae2", "covered_dense_cable")));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptional(ResourceLocation.fromNamespaceAndPath("ae2", "quantum_ring"));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptional(ResourceLocation.fromNamespaceAndPath("ae2", "quantum_link"));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptional(ResourceLocation.fromNamespaceAndPath("ae2", "quartz_fiber"));
        tag(ModTags.Blocks.VALID_DATACENTER_INNER_BLOCK).addOptional(ResourceLocation.fromNamespaceAndPath("torchmaster", "invisible_light"));
    }
}
