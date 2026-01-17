package dev.gacbl.logicore.blocks.compiler.ui;

import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import dev.gacbl.logicore.network.payload.SetAutoCraftingTemplatePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class CompilerScreen extends MyAbstractContainerScreen<CompilerMenu> {

    public CompilerScreen(CompilerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        setTexture("textures/gui/compiler_ui.png");
        this.renderInventoryLabel = false;
        this.titleLabelX = leftPos + 90;
        this.titleLabelY = topPos + 14;
        this.inventoryLabelX = leftPos + 30;
        this.inventoryLabelY = topPos + 140;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            float percent = (float) progress / maxProgress;

            if (percent > 0) {
                float localPercent = Math.min(1.0f, percent / 0.36f);
                int width = (int) (20 * localPercent);
                if (width > 0) {
                    graphics.blit(TEXTURE, x + 89, y + 87, 232, 0, width, 5);
                }
            }

            if (percent > 0.36f) {
                float localPercent = Math.min(1.0f, (percent - 0.36f) / 0.27f);
                int width = (int) (15 * localPercent);
                if (width > 0) {
                    graphics.blit(TEXTURE, x + 109, y + 82, 232, 12, width, 15);
                }
            }

            if (percent > 0.63f) {
                float localPercent = (percent - 0.63f) / 0.37f;
                int width = (int) (20 * localPercent);
                if (width > 0) {
                    graphics.blit(TEXTURE, x + 124, y + 87, 232, 6, width, 5);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot hovered = this.hoveredSlot;

        if (hovered != null && hovered.index == CompilerMenu.TEMPLATE_SLOT_INDEX) {
            ItemStack carried = this.menu.getCarried();

            PacketDistributor.sendToServer(new SetAutoCraftingTemplatePayload(
                    this.menu.getBlockEntity().getBlockPos(),
                    carried
            ));

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
