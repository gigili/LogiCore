package dev.gacbl.logicore.blocks.datacenter.ui;

import dev.gacbl.logicore.blocks.datacenter.DatacenterControllerBlockEntity;
import dev.gacbl.logicore.blocks.datacenter.DatacenterModule;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DatacenterControllerMenu extends MyAbstractContainerMenu {
    public DatacenterControllerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(1));
    }

    public DatacenterControllerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(DatacenterModule.DATACENTER_CONTROLLER_MENU.get(), containerId, playerInventory, entity, data);
        this.TE_INVENTORY_SLOT_COUNT = 1;
        this.addSlot(new SlotItemHandler(((DatacenterControllerBlockEntity) this.blockEntity).getItemHandler(), 0, 108, 82));
    }

    public DatacenterControllerMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }
}
