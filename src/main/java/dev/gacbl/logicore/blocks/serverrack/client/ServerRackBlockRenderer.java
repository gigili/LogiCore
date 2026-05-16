package dev.gacbl.logicore.blocks.serverrack.client;

import com.geckolib.renderer.GeoBlockRenderer;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ServerRackBlockRenderer extends GeoBlockRenderer<ServerRackBlockEntity, ServerRackRenderState> {
    public ServerRackBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new ServerRackBlockModel());
    }

    @Override
    public ServerRackRenderState createRenderState() {
        return new ServerRackRenderState();
    }

    @Override
    public @Nullable RenderType getRenderType(ServerRackRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutoutCull(texture);
    }
}
