package dev.gacbl.logicore.blocks.compiler;

import dev.gacbl.logicore.api.computation.ICycleConsumer;
import dev.gacbl.logicore.blocks.compiler.recipe.CompilerRecipe;
import dev.gacbl.logicore.blocks.compiler.recipe.CompilerRecipeInput;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerMenu;
import dev.gacbl.logicore.blocks.datacable.DataCableBlockEntity;
import dev.gacbl.logicore.blocks.datacable.cable_network.ComputationNetwork;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class CompilerBlockEntity extends BlockEntity implements ICycleConsumer, MenuProvider {
    private static final int MAX_PROGRESS = 10;
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private long currentCycles = 0;
    private int progress = 0;
    private CompilerRecipe recipe;

    public int getProgress() {
        return progress;
    }

    private RecipeHolder<CompilerRecipe> cachedRecipe = null;

    public CompilerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CompilerModule.COMPILER_BLOCK_ENTITY.get(), pos, blockState);
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == INPUT_SLOT && !this.getStackInSlot(INPUT_SLOT).isEmpty()) {
                requestCycles();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT;
        }
    };

    private final IItemHandler automationInputHandler = new RangedWrapper(itemHandler, INPUT_SLOT, INPUT_SLOT + 1) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };

    private final IItemHandler automationOutputHandler = new RangedWrapper(itemHandler, OUTPUT_SLOT, OUTPUT_SLOT + 1) {
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }
    };

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return itemHandler;
        }

        if (side == Direction.DOWN) {
            return automationOutputHandler;
        }

        return automationInputHandler;
    }

    @Override
    public long getCyclesStored() {
        return this.currentCycles;
    }

    @Override
    public long getCycleDemand() {
        RecipeHolder<CompilerRecipe> recipe = getRecipe();
        if (recipe != null && canInsertOutput(recipe.value())) {
            return recipe.value().cycles();
        }
        return 0;
    }

    @Override
    public long receiveCycles(long maxReceive, boolean simulate) {
        RecipeHolder<CompilerRecipe> recipe = getRecipe();
        if (recipe == null) return 0;

        long required = recipe.value().cycles();
        long accepted = Math.min(maxReceive, required);

        if (!simulate) {
            this.currentCycles += accepted;
            setChanged();
        }
        return accepted;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CompilerBlockEntity be) {
        if (level.isClientSide) return;

        RecipeHolder<CompilerRecipe> recipeHolder = be.getRecipe();

        if (recipeHolder != null && be.canInsertOutput(recipeHolder.value())) {
            be.recipe = recipeHolder.value();

            if (be.currentCycles >= be.recipe.cycles()) {
                be.progress++;
                be.currentCycles -= be.recipe.cycles();

                if (be.progress >= be.recipe.getTime()) {
                    be.craftItem(be.recipe);
                    be.progress = 0;
                }
                be.setChanged();
            }
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.recipe = null;
                be.setChanged();
            }
        }
    }

    public RecipeHolder<CompilerRecipe> getRecipe() {
        if (this.level == null) return null;

        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return null;

        if (cachedRecipe != null && cachedRecipe.value().matches(new CompilerRecipeInput(input), level)) {
            return cachedRecipe;
        }

        Optional<RecipeHolder<CompilerRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(CompilerModule.COMPILER_TYPE.get(), new CompilerRecipeInput(input), level);

        cachedRecipe = recipe.orElse(null);
        return cachedRecipe;
    }

    private boolean canInsertOutput(CompilerRecipe recipe) {
        if (level == null) return false;
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (outputStack.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(outputStack, result)) return false;

        return outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize();
    }

    private void craftItem(CompilerRecipe recipe) {
        if (level == null) return;

        itemHandler.extractItem(INPUT_SLOT, recipe.inputCount(), false);

        if (recipe.chance() < 1f) {
            if (this.level.random.nextFloat() <= recipe.chance()) {
                craftItems(recipe);
            }
            return;
        }

        craftItems(recipe);
    }

    private void craftItems(CompilerRecipe recipe) {
        if (level == null) return;
        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();

        ItemStack existing = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            existing.grow(result.getCount());
        }
    }

    public void dropContents() {
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemHandler.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putLong("currentCycles", currentCycles);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = tag.getInt("progress");
        currentCycles = tag.getLong("currentCycles");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.logicore.compiler");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new CompilerMenu(containerId, playerInventory, this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @Nullable ICycleConsumer getCycleStorage() {
        return this;
    }

    private void requestCycles() {
        if (level == null || level.isClientSide || level.getServer() == null) return;

        NetworkManager manager = NetworkManager.get(level.getServer().overworld());

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof DataCableBlockEntity dcbe) {
                UUID networkID = dcbe.getNetworkUUID();
                if (networkID == null) continue;

                if (manager.getNetworks().containsKey(networkID)) {
                    ComputationNetwork network = manager.getNetworks().get(networkID);
                    network.setDirty();
                    break;
                }
            }
        }
    }
}
