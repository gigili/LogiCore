package dev.gacbl.logicore.blocks.serverrack.client;

import com.geckolib.renderer.GeoBlockRenderer;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

//
public class ServerRackBlockRenderer extends GeoBlockRenderer<ServerRackBlockEntity, @NonNull ServerRackRenderState> {
    public ServerRackBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new ServerRackBlockModel());
    }

    @Override
    public @NonNull ServerRackRenderState createRenderState() {
        return new ServerRackRenderState();
    }

    @Override
    public @Nullable RenderType getRenderType(@NonNull ServerRackRenderState renderState, @NonNull Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }
}
