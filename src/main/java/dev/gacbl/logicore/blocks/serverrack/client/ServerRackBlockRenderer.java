package dev.gacbl.logicore.blocks.serverrack.client;

import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ServerRackBlockRenderer extends GeoBlockRenderer<ServerRackBlockEntity> {
    public ServerRackBlockRenderer() {
        super(new ServerRackBlockModel());
    }
}
