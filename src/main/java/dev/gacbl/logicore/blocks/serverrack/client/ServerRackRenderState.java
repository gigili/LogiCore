package dev.gacbl.logicore.blocks.serverrack.client;

import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class ServerRackRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState[] slotItems = new ItemStackRenderState[ServerRackBlockEntity.RACK_CAPACITY];
    public Direction facing = Direction.NORTH;

    public ServerRackRenderState() {
        for (int i = 0; i < slotItems.length; i++) {
            slotItems[i] = new ItemStackRenderState();
        }
    }
}
