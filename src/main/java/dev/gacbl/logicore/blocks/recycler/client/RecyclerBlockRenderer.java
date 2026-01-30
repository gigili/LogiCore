package dev.gacbl.logicore.blocks.recycler.client;

import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RecyclerBlockRenderer extends GeoBlockRenderer<RecyclerBlockEntity> {
    public RecyclerBlockRenderer() {
        super(new RecyclerBlockModel());
    }
}
