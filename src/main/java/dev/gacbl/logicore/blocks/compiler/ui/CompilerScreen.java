package dev.gacbl.logicore.blocks.compiler.ui;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.network.payload.SetAutoCraftingTemplatePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

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
        this.imageWidth = 175;
        this.imageHeight = 188;

        this.titleLabelX = leftPos + 7;
        this.titleLabelY = topPos + 5;

        this.inventoryLabelX = leftPos + 7;
        this.inventoryLabelY = topPos + 96;
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
        graphics.blit(TEXTURE, x, y, 0, 0, 175, 188);

        if (menu.getProgress() > 0) {
            int progress = menu.getProgress();
            int maxProgress = menu.getMaxProgress();
            int arrowSize = 47;
            graphics.blit(TEXTURE, x + 63, y + 36, 176, 0, progress * arrowSize / maxProgress, 29);
        }
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
