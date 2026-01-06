package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record SyncPlayerKnowledgePayload(String itemKey) implements CustomPacketPayload {
    public static final Type<SyncPlayerKnowledgePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_player_knowledge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerKnowledgePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncPlayerKnowledgePayload::itemKey,
            SyncPlayerKnowledgePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SyncPlayerKnowledgePayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            ClientKnowledgeData.add(payload.itemKey());
        });
    };
}
