package dev.gacbl.logicore.blocks.recycler.client;

import com.geckolib.renderer.GeoBlockRenderer;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class RecyclerBlockRenderer extends GeoBlockRenderer<RecyclerBlockEntity, BlockEntityRenderState> {
    public RecyclerBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new RecyclerBlockModel());
    }
}
