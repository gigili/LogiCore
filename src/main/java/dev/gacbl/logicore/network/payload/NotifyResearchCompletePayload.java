package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record NotifyResearchCompletePayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<NotifyResearchCompletePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "notify_research_complete"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NotifyResearchCompletePayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, NotifyResearchCompletePayload::stack,
            NotifyResearchCompletePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
