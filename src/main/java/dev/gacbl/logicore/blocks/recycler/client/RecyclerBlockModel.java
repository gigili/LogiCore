package dev.gacbl.logicore.blocks.recycler.client;

import com.geckolib.model.DefaultedBlockGeoModel;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.recycler.RecyclerBlockEntity;
import net.minecraft.resources.Identifier;

public class RecyclerBlockModel extends DefaultedBlockGeoModel<RecyclerBlockEntity> {

    public RecyclerBlockModel() {
        super(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "recycler"));
    }
}
