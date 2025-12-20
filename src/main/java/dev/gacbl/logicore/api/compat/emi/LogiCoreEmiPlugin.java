package dev.gacbl.logicore.api.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.compiler.ui.CompilerScreen;
import dev.gacbl.logicore.network.payload.SetAutoCraftingTemplatePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EmiEntrypoint
public class LogiCoreEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!Config.ALLOW_JEI_DRAG.get()) return;

        registry.addDragDropHandler(CompilerScreen.class, new CompilerDragDropHandler());
    }

    private static class CompilerDragDropHandler implements EmiDragDropHandler<CompilerScreen> {
        private static final int SLOT_X_REL = 72;
        private static final int SLOT_Y_REL = 82;
        private static final int SLOT_SIZE = 16;

        @Override
        public boolean dropStack(CompilerScreen screen, EmiIngredient ingredient, int x, int y) {
            int slotX = screen.getGuiLeft() + SLOT_X_REL;
            int slotY = screen.getGuiTop() + SLOT_Y_REL;

            if (x >= slotX && x < slotX + SLOT_SIZE && y >= slotY && y < slotY + SLOT_SIZE) {
                List<EmiStack> stacks = ingredient.getEmiStacks();
                if (stacks.isEmpty()) return false;

                PacketDistributor.sendToServer(new SetAutoCraftingTemplatePayload(
                        screen.getMenu().blockEntity.getBlockPos(),
                        stacks.getFirst().getItemStack()
                ));

                return true;
            }

            return false;
        }

        @Override
        public void render(CompilerScreen screen, EmiIngredient ingredient, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int slotX = screen.getGuiLeft() + SLOT_X_REL;
            int slotY = screen.getGuiTop() + SLOT_Y_REL;

            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x8800FF00);
            }
        }
    }
}
