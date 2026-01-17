package dev.gacbl.logicore.blocks.research_station.ui;

import dev.gacbl.logicore.blocks.research_station.ResearchStationBlockEntity;
import dev.gacbl.logicore.blocks.research_station.ResearchStationModule;
import dev.gacbl.logicore.core.ui.MyAbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ResearchStationMenu extends MyAbstractContainerMenu {
    public ResearchStationMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(pos), new SimpleContainerData(2));
    }

    public ResearchStationMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ResearchStationModule.RESEARCH_STATION_MENU.get(), containerId, playerInventory, entity, data);
        ResearchStationMenu.TE_INVENTORY_SLOT_COUNT = 1;
        this.addSlot(new SlotItemHandler(((ResearchStationBlockEntity) this.blockEntity).getItemHandler(), 0, 107, 82));
    }

    public ResearchStationMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, inventory, registryFriendlyByteBuf.readBlockPos());
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }
}
