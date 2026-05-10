package dev.gacbl.logicore.blocks.serverrack.client;

import com.geckolib.renderer.GeoBlockRenderer;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class ServerRackBlockRenderer extends GeoBlockRenderer<ServerRackBlockEntity, BlockEntityRenderState> {
    public ServerRackBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new ServerRackBlockModel());
    }
}
