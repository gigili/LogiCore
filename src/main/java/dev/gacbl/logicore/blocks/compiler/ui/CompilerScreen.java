package dev.gacbl.logicore.blocks.compiler.ui;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.network.payload.SetAutoCraftingTemplatePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class CompilerScreen extends AbstractContainerScreen<CompilerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/gui/compiler_ui.png");

    public int getGuiLeft() {
        return this.leftPos;
    }

    public int getGuiTop() {
        return this.topPos;
    }

    public CompilerScreen(CompilerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 231;
        this.imageHeight = 243;

        this.titleLabelX = leftPos + 90;
        this.titleLabelY = topPos + 14;

        this.inventoryLabelX = leftPos + 30;
        this.inventoryLabelY = topPos + 140;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

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
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), this.titleLabelX, this.titleLabelY, 10461087, true);
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 7303023, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot hovered = this.hoveredSlot;

        if (hovered != null && hovered.index == CompilerMenu.TEMPLATE_SLOT_INDEX) {
            ItemStack carried = this.menu.getCarried();

            PacketDistributor.sendToServer(new SetAutoCraftingTemplatePayload(
                    this.menu.blockEntity.getBlockPos(),
                    carried
            ));

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
