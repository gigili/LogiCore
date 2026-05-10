package dev.gacbl.logicore.blocks.serverrack.client;

import com.geckolib.model.DefaultedBlockGeoModel;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.serverrack.ServerRackBlockEntity;
import net.minecraft.resources.Identifier;

public class ServerRackBlockModel extends DefaultedBlockGeoModel<ServerRackBlockEntity> {
    public ServerRackBlockModel() {
        super(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "server_rack"));
    }
}
