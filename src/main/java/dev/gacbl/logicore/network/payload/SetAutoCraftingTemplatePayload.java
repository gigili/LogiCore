package dev.gacbl.logicore.network.payload;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.compiler.CompilerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record SetAutoCraftingTemplatePayload(BlockPos pos, ItemStack stack) implements CustomPacketPayload {

    public static final Type<SetAutoCraftingTemplatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "set_auto_crafting_template"));

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
                    } else {
                        be.getItemHandler(null).getStackInSlot(0).copyAndClear();
                        be.setChanged();
                        return;
                    }

                    be.getItemHandler(null).insertItem(CompilerBlockEntity.INPUT_SLOT, template, false);
                    be.setChanged();
                }
            }
        });
    };
}
