package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.client.ResearchToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
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

    public static final IPayloadHandler<NotifyResearchCompletePayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            Minecraft.getInstance().getToasts().addToast(new ResearchToast(payload.stack()));
        });
    };
}
