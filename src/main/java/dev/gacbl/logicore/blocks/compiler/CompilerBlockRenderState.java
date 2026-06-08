package dev.gacbl.logicore.blocks.compiler;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class CompilerBlockRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public final ItemStackRenderState item = new ItemStackRenderState();
    public boolean isWorking;
    public float renderRotation;
    public float progress;
    public long time;
}
