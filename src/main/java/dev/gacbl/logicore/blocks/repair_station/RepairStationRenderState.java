package dev.gacbl.logicore.blocks.repair_station;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RepairStationRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public final ItemStackRenderState item = new ItemStackRenderState();
    public boolean isRepairing;
    public int progress;
    public int maxProgress;
    public boolean hasStack;
    public String itemName = "";
    public int damageValue;
    public int maxDamage;
    public BlockPos blockPos;
    public long gameTime;
    public float partialTick;
    public boolean isBlockItem;
}
