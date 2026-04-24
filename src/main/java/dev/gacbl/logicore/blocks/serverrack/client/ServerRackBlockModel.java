package dev.gacbl.logicore.blocks.serverrack.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class ServerRackBlockModel extends DefaultedBlockGeoModel<ServerRackBlockEntity> {
    public ServerRackBlockModel() {
        super(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "server_rack"));
    }

    @Override
    public RenderType getRenderType(ServerRackBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
