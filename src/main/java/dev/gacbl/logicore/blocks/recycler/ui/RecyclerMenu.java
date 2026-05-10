package dev.gacbl.logicore.blocks.recycler.ui;

import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import dev.gacbl.logicore.blocks.recycler.RecyclerModule;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class RecyclerMenu extends MyAbstractContainerMenu {
    public RecyclerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(2));
    }

    public RecyclerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(RecyclerModule.RECYCLER_MENU.get(), containerId, playerInventory, entity, data);
        this.TE_INVENTORY_SLOT_COUNT = 2;
        var handler = ((RecyclerBlockEntity) this.blockEntity).getItemHandler();
        this.addSlot(new ResourceHandlerSlot(handler, handler::set, 0, 90, 82));
        this.addSlot(new ResourceHandlerSlot(handler, handler::set, 1, 125, 82));
    }

    public RecyclerMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }
}
