package dev.gacbl.logicore.blocks.generator.ui;

import dev.gacbl.logicore.blocks.generator.GeneratorBlockEntity;
import dev.gacbl.logicore.blocks.generator.GeneratorModule;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GeneratorMenu extends MyAbstractContainerMenu {
    public GeneratorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(12));
    }

    public GeneratorMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    public GeneratorMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(GeneratorModule.GENERATOR_MENU.get(), containerId, playerInventory, entity, data);
        GeneratorMenu.TE_INVENTORY_SLOT_COUNT = 3;

        for (int row = 0; row < 3; row++) {
            this.addSlot(new SlotItemHandler(((GeneratorBlockEntity) this.blockEntity).getItemHandler(null), row, 90 + row * 18, 103));
        }
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
}
