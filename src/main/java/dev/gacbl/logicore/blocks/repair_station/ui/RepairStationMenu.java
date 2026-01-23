package dev.gacbl.logicore.blocks.repair_station.ui;

import dev.gacbl.logicore.blocks.repair_station.RepairStationBlockEntity;
import dev.gacbl.logicore.blocks.repair_station.RepairStationModule;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RepairStationMenu extends MyAbstractContainerMenu {
    public RepairStationMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(2));
    }

    public RepairStationMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(RepairStationModule.REPAIR_STATION_MENU.get(), containerId, playerInventory, entity, data);
        this.TE_INVENTORY_SLOT_COUNT = 1;
        this.addSlot(new SlotItemHandler(((RepairStationBlockEntity) this.blockEntity).getItemHandler(), 0, 107, 82));
    }

    public RepairStationMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }
}
