package dev.gacbl.logicore.blocks.datacenter;

import dev.gacbl.logicore.blocks.computer.ComputerModule;
import dev.gacbl.logicore.blocks.datacable.DataCableModule;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import dev.gacbl.logicore.multiblock.AbstractSealedController;
import dev.gacbl.logicore.multiblock.MultiblockValidationException;
import dev.gacbl.logicore.multiblock.MultiblockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DatacenterControllerBlockEntity extends AbstractSealedController {

    public DatacenterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DatacenterModule.DATACENTER_CONTROLLER_BE.get(), pos, state);
    }

    @Override
    protected boolean isFrameBlock(BlockState state) {
        return state.is(Blocks.IRON_BLOCK)
                || state.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "obsidians")));
    }

    @Override
    protected boolean isWallBlock(BlockState state) {
        return state.is(Blocks.IRON_BLOCK)
                || state.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "glass_blocks")))
                || state.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "obsidians")))
                || state.is(Blocks.IRON_DOOR)
                || state.is(DatacenterModule.DATACENTER_CONTROLLER.get())
                || state.is(Blocks.GLOWSTONE)
                || state.is(Blocks.REDSTONE_LAMP);
    }

    @Override
    protected boolean isInteriorBlock(BlockState state) {
        return state.isAir()
                || state.is(BlockTags.PRESSURE_PLATES)
                || state.is(BlockTags.BUTTONS)
                || state.is(Blocks.LEVER)
                || state.is(ServerRackModule.SERVER_RACK_BLOCK.get())
                || state.is(DataCableModule.DATA_CABLE_BLOCK.get())
                || state.is(ComputerModule.COMPUTER_BLOCK.get())
                || state.is(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("justdirethings", "generatort1")));
    }

    private boolean isControllerBlock(BlockState state) {
        return state.getBlock() == DatacenterModule.DATACENTER_CONTROLLER.get();
    }

    public void attemptFormation() {
        if (level == null || level.isClientSide) return;

        Direction facing = this.getBlockState().getValue(BlockStateProperties.FACING);

        try {
            MultiblockValidator.ValidationResult result = MultiblockValidator.detectRoom(
                    level,
                    worldPosition,
                    facing,
                    this::isFrameBlock,
                    this::isWallBlock,
                    this::isInteriorBlock,
                    this::isControllerBlock,
                    7,
                    32
            );

            // If detectRoom returns (doesn't throw), it was successful
            formStructure(result.min, result.max);

        } catch (MultiblockValidationException e) {
            // Capture the specific error (e.g., "Duplicate Controller at X,Y,Z")
            breakStructure(e);
        }
    }

    @Override
    protected void onStructureFormed() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(DatacenterControllerBlock.FORMED, true), 3);
        }
    }

    @Override
    protected void onStructureBroken() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(DatacenterControllerBlock.FORMED, false), 3);
        }
    }
}
