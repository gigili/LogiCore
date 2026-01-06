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

import java.util.List;

public record SyncAllPlayerKnowledgePayload(List<String> itemKeys) implements CustomPacketPayload {
    public static final Type<SyncAllPlayerKnowledgePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_all_player_knowledge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAllPlayerKnowledgePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncAllPlayerKnowledgePayload::itemKeys,
            SyncAllPlayerKnowledgePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SyncAllPlayerKnowledgePayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            ClientKnowledgeData.addAll(payload.itemKeys());
        });
    };
}
