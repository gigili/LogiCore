package dev.gacbl.logicore.blocks.generator;

import dev.gacbl.logicore.blocks.generator.ui.GeneratorMenu;
import dev.gacbl.logicore.blocks.serverrack.ServerRackModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {
    private final EnergyStorage energyStorage = new EnergyStorage(1_000_000, 10000, 10000);

    public int[] burnDuration = new int[]{0, 0, 0};
    public int[] maxBurnDuration = new int[]{0, 0, 0};
    public int[] feGenerationRate = new int[]{0, 0, 0};

    public boolean isGenerating = false;

    public GeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GeneratorModule.GENERATOR_BE.get(), pos, blockState);
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getBurnTime(RecipeType.SMELTING) > 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    };

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null || side == Direction.UP) return itemHandler;
        return null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new GeneratorMenu(containerId, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("energyStorage", energyStorage.serializeNBT(registries));
        tag.putBoolean("isGenerating", isGenerating);

        tag.putIntArray("burnDuration", burnDuration);
        tag.putIntArray("maxBurnDuration", maxBurnDuration);
        tag.putIntArray("feGenerationRate", feGenerationRate);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }

        if (tag.contains("energyStorage", 3) && tag.get("energyStorage") != null) {
            this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("energyStorage")));
        }

        isGenerating = tag.getBoolean("isGenerating");

        if (tag.contains("burnDuration")) burnDuration = tag.getIntArray("burnDuration");
        if (tag.contains("maxBurnDuration")) maxBurnDuration = tag.getIntArray("maxBurnDuration");
        if (tag.contains("feGenerationRate")) feGenerationRate = tag.getIntArray("feGenerationRate");

        if (burnDuration.length != 3) burnDuration = new int[]{0, 0, 0};
        if (maxBurnDuration.length != 3) maxBurnDuration = new int[]{0, 0, 0};
        if (feGenerationRate.length != 3) feGenerationRate = new int[]{0, 0, 0};
    }

    public void dropContents() {
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(i));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (level == null) return;

        boolean wasGenerating = be.isGenerating;
        boolean currentlyGenerating = false;

        distributeEnergy(level, pos, be);

        if (be.energyStorage.getEnergyStored() >= be.energyStorage.getMaxEnergyStored()) {
            currentlyGenerating = false;
            setChanged(level, pos, state);
            be.isGenerating = currentlyGenerating;
            level.setBlock(pos, state.setValue(ServerRackModule.GENERATING, be.isGenerating), 3);
            return;
        }

        for (int i = 0; i < 3; i++) {
            if (be.burnDuration[i] > 0) {
                be.energyStorage.receiveEnergy(be.feGenerationRate[i], false);
                be.burnDuration[i]--;
                currentlyGenerating = true;
                setChanged(level, pos, state);
            }

            if (be.burnDuration[i] <= 0) {
                ItemStack stack = be.itemHandler.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    int potentialFe = getFePerTick(stack);
                    int maxStorage = be.energyStorage.getMaxEnergyStored();
                    int currentStorage = be.energyStorage.getEnergyStored();

                    if (currentStorage + potentialFe <= maxStorage) {
                        int burnTime = stack.getBurnTime(RecipeType.SMELTING);

                        if (burnTime > 0) {
                            be.burnDuration[i] = burnTime;
                            be.maxBurnDuration[i] = burnTime;
                            be.feGenerationRate[i] = potentialFe;

                            stack.shrink(1);
                            be.itemHandler.setStackInSlot(i, stack);

                            // Immediately generate for this tick so there is no gap
                            be.energyStorage.receiveEnergy(potentialFe, false);
                            be.burnDuration[i]--;
                            currentlyGenerating = true;
                            setChanged(level, pos, state);
                        }
                    }
                }
            }
        }

        if (wasGenerating != currentlyGenerating) {
            be.isGenerating = currentlyGenerating;
            level.setBlock(pos, state.setValue(ServerRackModule.GENERATING, be.isGenerating), 3);
            setChanged(level, pos, state);
        }
    }

    private static void distributeEnergy(Level level, BlockPos pos, GeneratorBlockEntity be) {
        if (be.energyStorage.getEnergyStored() <= 0) return;

        Direction facing = be.getBlockState().getValue(GeneratorBlock.FACING);

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == facing) continue;

            BlockPos neighborPos = pos.relative(direction);
            IEnergyStorage neighborEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());

            if (neighborEnergy != null && neighborEnergy.canReceive()) {
                int extracted = be.energyStorage.extractEnergy(be.energyStorage.getMaxEnergyStored(), true);
                int accepted = neighborEnergy.receiveEnergy(extracted, false);
                be.energyStorage.extractEnergy(accepted, false);
                if (be.energyStorage.getEnergyStored() <= 0) break;
            }
        }
    }

    private static int getFePerTick(ItemStack stack) {
        if (stack.is(ItemTags.COALS)) return 120; // Coal/Charcoal
        if (stack.is(Items.COAL_BLOCK)) return 1200;
        if (stack.is(ItemTags.LOGS)) return 60;   // Logs
        if (stack.is(ItemTags.PLANKS)) return 40; // Planks
        if (stack.is(Items.BLAZE_ROD)) return 200;

        int burnTime = stack.getBurnTime(RecipeType.SMELTING);
        return Math.max(10, burnTime / 10);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> GeneratorBlockEntity.this.energyStorage.getEnergyStored();
                case 1 -> GeneratorBlockEntity.this.energyStorage.getMaxEnergyStored();
                case 2 -> GeneratorBlockEntity.this.isGenerating ? 1 : 0;
                case 3 -> GeneratorBlockEntity.this.burnDuration[0];
                case 4 -> GeneratorBlockEntity.this.maxBurnDuration[0];
                case 5 -> GeneratorBlockEntity.this.burnDuration[1];
                case 6 -> GeneratorBlockEntity.this.maxBurnDuration[1];
                case 7 -> GeneratorBlockEntity.this.burnDuration[2];
                case 8 -> GeneratorBlockEntity.this.maxBurnDuration[2];
                case 9 -> GeneratorBlockEntity.this.feGenerationRate[0];
                case 10 -> GeneratorBlockEntity.this.feGenerationRate[1];
                case 11 -> GeneratorBlockEntity.this.feGenerationRate[2];
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 12;
        }
    };
}
