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
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class DatacenterControllerMenu extends MyAbstractContainerMenu {
    public DatacenterControllerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(3));
    }

    public DatacenterControllerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(DatacenterModule.DATACENTER_CONTROLLER_MENU.get(), containerId, playerInventory, entity, data);
        this.TE_INVENTORY_SLOT_COUNT = 1;

        var handler = ((DatacenterControllerBlockEntity) this.blockEntity).getItemHandler();
        this.addSlot(new ResourceHandlerSlot(handler, handler::set, 0, 108, 82));
    }

    public DatacenterControllerMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    public int getProcessors() {
        return this.data.get(0);
    }

    public int getServers() {
        return this.data.get(1);
    }

    public int getPorts() {
        return this.data.get(2);
    }
}