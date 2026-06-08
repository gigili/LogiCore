package dev.gacbl.logicore.blocks.research_station;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class ResearchStationRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public final ItemStackRenderState item = new ItemStackRenderState();
    public boolean isBlockItem;
}
