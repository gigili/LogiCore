package dev.gacbl.logicore.core;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> VALID_DATACENTER_WALL_BLOCK = createTag("valid_datacenter_wall_block");
        public static final TagKey<Block> VALID_DATACENTER_FRAME_BLOCK = createTag("valid_datacenter_frame_block");
        public static final TagKey<Block> VALID_DATACENTER_INNER_BLOCK = createTag("valid_datacenter_inner_block");
        public static final TagKey<Block> IS_ENERGY_GENERATOR = createTag("is_energy_generator");
        public static final TagKey<Block> IS_ENERGY_CABLE = createTag("is_energy_cable");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, name));
        }
    }
}
