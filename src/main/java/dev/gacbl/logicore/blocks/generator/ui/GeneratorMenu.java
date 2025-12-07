package dev.gacbl.logicore.blocks.generator.ui;

import dev.gacbl.logicore.blocks.generator.GeneratorBlockEntity;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class GeneratorMenu extends AbstractContainerMenu {
    public final GeneratorBlockEntity blockEntity;
    private final ContainerData data;
    private final Level level;

    public GeneratorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(12));
    }

    public GeneratorMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(GeneratorModule.GENERATOR_MENU.get(), containerId);
        this.blockEntity = (GeneratorBlockEntity) entity;
        this.data = data;
        this.level = playerInventory.player.level();

        int startX = 90;
        int startY = 103;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        for (int row = 0; row < 3; row++) {
            this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(null), row, startX + row * 18, startY));
        }

        addDataSlots(this.data);
    }

    public GeneratorMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 39 = TileInventory slots, which map to our TileEntity slot numbers 0 - 2)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 3;  // must be the number of slots you have!

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, GeneratorModule.GENERATOR.get());
    }

    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    public boolean isGenerating() {
        return this.data.get(2) == 1;
    }

    public int getBurnProgress(int slotIndex, int pixelHeight) {
        int dataBaseIndex = 3 + (slotIndex * 2);

        int current = this.data.get(dataBaseIndex);
        int max = this.data.get(dataBaseIndex + 1);

        if (max == 0 || current == 0) {
            return 0;
        }

        return current * pixelHeight / max;
    }

    public boolean isSlotBurning(int slotIndex) {
        int dataBaseIndex = 3 + (slotIndex * 2);
        return this.data.get(dataBaseIndex) > 0;
    }

    public int getCurrentGenerationRate() {
        int total = 0;
        for (int i = 0; i < 3; i++) {
            total += this.getCurrentGenerationRateForSlot(i);
        }
        return total;
    }

    public int getCurrentGenerationRateForSlot(int slotIndex) {
        int burnIndex = 3 + (slotIndex * 2);
        int rateIndex = 9 + slotIndex;
        if (this.data.get(burnIndex) > 0) {
            return this.data.get(rateIndex);
        }
        return 0;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 36 + l * 18, 157 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 36 + i * 18, 213));
        }
    }
}
