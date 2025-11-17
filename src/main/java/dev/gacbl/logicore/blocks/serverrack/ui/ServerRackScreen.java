package dev.gacbl.logicore.blocks.serverrack.ui;

import dev.gacbl.logicore.LogiCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ServerRackScreen extends AbstractContainerScreen<dev.gacbl.logicore.blocks.serverrack.ui.ServerRackMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/gui/server_rack_gui_v2.png");

    public ServerRackScreen(ServerRackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 175;
        this.imageWidth = 188;
        this.inventoryLabelY = 95;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, 175, 188);

        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = 69;
            int barWidth = 15;
            int textureTotalHeight = 75;

            int filledHeight = (int) (((float) energy / maxEnergy) * barHeight);

            if (filledHeight > 0) {
                int yOffset = barHeight - filledHeight;
                int vOffset = textureTotalHeight - filledHeight;

                graphics.blit(TEXTURE,
                        leftPos + 8, topPos + 19 + yOffset, // Screen Position (X, Y)
                        176, vOffset,                       // Texture Coordinates (U, V)
                        barWidth, filledHeight,             // Size (Width, Height)
                        256, 256                            // Texture Sheet Size
                );
            }
        }
    }

    private void renderScaledText(GuiGraphics graphics, Component text, int x, int y, float scale, int color) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, 0, 0, color, false);
        pose.popPose();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);

        int labelSectionX = leftPos + 118;
        int labelSectionY = topPos + 29;
        int cycles = this.menu.getCycles();
        int maxCycles = this.menu.getMaxCycles();

        float scale = 0.90f;
        int lineSpacing = 8;

        renderScaledText(graphics, Component.literal("Cycles: "), labelSectionX, labelSectionY, scale, 4210752);

        labelSectionY += lineSpacing;

        Component cyclesText = Component.literal(formatEnergy(cycles));
        renderScaledText(graphics, cyclesText, labelSectionX, labelSectionY, scale, 4210752);

        labelSectionY += lineSpacing + 14;

        renderScaledText(graphics, Component.literal("Max: "), labelSectionX, labelSectionY, scale, 4210752);

        labelSectionY += lineSpacing;

        Component maxCyclesText = Component.literal(formatEnergy(maxCycles));
        renderScaledText(graphics, maxCyclesText, labelSectionX, labelSectionY, scale, 4210752);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int powerSectionX = leftPos + 8;
        int powerSectionY = topPos + 19;

        if (mouseX >= powerSectionX && mouseY >= powerSectionY && mouseX <= powerSectionX + 15 && mouseY <= powerSectionY + 70) {
            int current = this.menu.getEnergy();
            int max = this.menu.getMaxEnergy();
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.logicore.energy_stored", formatEnergy(current), formatEnergy(max)), mouseX, mouseY);
        }
    }

    private String formatEnergy(long energy) {
        if (energy >= 1_000_000) {
            return String.format("%.1fM", energy / 1_000_000.0);
        } else if (energy >= 1_000) {
            return String.format("%.1fK", energy / 1_000.0);
        }
        return String.valueOf(energy);
    }
}
