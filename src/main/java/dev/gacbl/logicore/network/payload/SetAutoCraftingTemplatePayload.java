package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public record SetAutoCraftingTemplatePayload(BlockPos pos, ItemStack stack) implements CustomPacketPayload {

    public static final Type<SetAutoCraftingTemplatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LogiCore.MOD_ID, "set_auto_crafting_template"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetAutoCraftingTemplatePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetAutoCraftingTemplatePayload::pos,
            ItemStack.OPTIONAL_STREAM_CODEC, SetAutoCraftingTemplatePayload::stack,
            SetAutoCraftingTemplatePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<SetAutoCraftingTemplatePayload> HANDLER = (payload, context) -> {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.pos()) instanceof CompilerBlockEntity be) {
                    ItemStack template = payload.stack().copy();

                    if (!template.isEmpty()) {
                        template.setCount(1);
                    }

                    ItemResource resource = be.getInternalItemHandler().getResource(0);
                    if (resource != null) {
                        try (Transaction tx = Transaction.openRoot()) {
                            be.getInternalItemHandler().extract(0, resource, 64, tx);
                            tx.commit();
                        }
                    }

                    if (template.isEmpty()) {
                        return;
                    }

                    try (Transaction tx = Transaction.openRoot()) {
                        be.getInternalItemHandler().insert(CompilerBlockEntity.INPUT_SLOT, ItemResource.of(template), template.getCount(), tx);
                        tx.commit();
                    }
                    be.setChanged();
                }
            }
        });
    };
}