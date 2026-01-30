package dev.gacbl.logicore.blocks.recycler.client;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class RecyclerBlockModel extends DefaultedBlockGeoModel<RecyclerBlockEntity> {

    public RecyclerBlockModel() {
        super(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "recycler"));
    }

    @Override
    public RenderType getRenderType(RecyclerBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
