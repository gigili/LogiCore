package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.CoreCycleProviderBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record SyncCycleDataPayload(BlockPos pos, int energy, long cycles,
                                   boolean isGenerating) implements CustomPacketPayload {

    public static final Type<SyncCycleDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "sync_cycle_data"));

    public static final StreamCodec<ByteBuf, SyncCycleDataPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncCycleDataPayload::pos,
            ByteBufCodecs.INT, SyncCycleDataPayload::energy,
            ByteBufCodecs.VAR_LONG, SyncCycleDataPayload::cycles,
            ByteBufCodecs.BOOL, SyncCycleDataPayload::isGenerating,
            SyncCycleDataPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SyncCycleDataPayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            // We are on the client here
            var level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity be = level.getBlockEntity(payload.pos());
                if (be instanceof CoreCycleProviderBlockEntity cycleBe) {
                    cycleBe.setClientData(payload.energy(), payload.cycles(), payload.isGenerating());
                }
            }
        });
    };
}
