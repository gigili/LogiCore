package dev.gacbl.logicore.blocks.generator.ui;

import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LogiCore.MOD_ID, "textures/gui/generator_ui.png");

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 231;
        this.imageHeight = 243;

        this.titleLabelX = leftPos + 80;
        this.titleLabelY = topPos + 14;

        this.inventoryLabelX = leftPos + 30;
        this.inventoryLabelY = topPos + 140;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, 231, 243);

        renderMainPowerBar(graphics);
        renderSidePowerBarAnimation(graphics);
        renderSideHorizontalPowerBarAnimation(graphics);
        renderFlames(graphics);
    }

    private void renderMainPowerBar(GuiGraphics graphics) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = 75;
            int barWidth = 16;
            int textureTotalHeight = 75;

            int filledHeight = (int) (((float) energy / maxEnergy) * barHeight);

            if (filledHeight > 0) {
                int yOffset = barHeight - filledHeight;
                int vOffset = textureTotalHeight - filledHeight;

                graphics.blit(TEXTURE,
                        leftPos + 36, topPos + 70 + yOffset, // Screen Position (X, Y)
                        237, 61 + vOffset,                       // Texture Coordinates (U, V)
                        barWidth, filledHeight,             // Size (Width, Height)
                        256, 256                            // Texture Sheet Size
                );
            }
        }
    }

    private void renderSidePowerBarAnimation(GuiGraphics graphics) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = 155;
            int barWidth = 5;
            int textureTotalHeight = 155;

            float rawRatio = (float) energy / maxEnergy;

            float scaledRatio = Math.min(rawRatio / 0.95f, 1.0f);

            int filledHeight = (int) (scaledRatio * barHeight);

            if (filledHeight > 0) {
                int yOffset = barHeight - filledHeight;
                int vOffset = textureTotalHeight - filledHeight + 56;

                graphics.blit(TEXTURE,
                        leftPos + 16, topPos + 61 + yOffset,
                        232, vOffset,
                        barWidth, filledHeight,
                        256, 256
                );
            }
        }
    }

    private void renderSideHorizontalPowerBarAnimation(GuiGraphics graphics) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();

        if (maxEnergy > 0 && energy > 0) {
            int barWidth = 7;
            int barHeight = 5;

            float rawRatio = (float) energy / maxEnergy;

            if (rawRatio > 0.95f) {
                int cpuSize = 15;
                float scaledRatioCpu = (rawRatio - 0.95f) / 0.05f;
                scaledRatioCpu = Math.min(scaledRatioCpu, 1.0f);

                int filledHeightCpu = (int) (scaledRatioCpu * cpuSize);

                if (filledHeightCpu > 0) {
                    graphics.blit(TEXTURE,
                            leftPos + 11, topPos + 46 + 15 - filledHeightCpu,  // X, Y (Fixed position)
                            232, 211 + 15 - filledHeightCpu,                    // U, V (Start of texture)
                            cpuSize, filledHeightCpu,
                            256, 256
                    );
                }
            }

            // Only render if we are in the top 5% of power (0.95 to 1.0)
            if (rawRatio > 0.97f) {
                // Normalize the 0.95-1.0 range to 0.0-1.0
                float scaledRatio = (rawRatio - 0.97f) / 0.02f;

                // Clamp to 1.0 in case of tiny overflows
                scaledRatio = Math.min(scaledRatio, 1.0f);

                int filledWidth = (int) (scaledRatio * barWidth);

                if (filledWidth > 0) {
                    graphics.blit(TEXTURE,
                            leftPos + 26, topPos + 51,  // X, Y (Fixed position)
                            237, 56,                    // U, V (Start of texture)
                            filledWidth, barHeight,     // Width grows, Height is fixed
                            256, 256
                    );
                }
            }
        }
    }

    private void renderFlames(GuiGraphics graphics) {
        int maxHeight = 13;

        int[] xOffsets = {91, 110, 127};

        for (int i = 0; i < 3; i++) {
            if (this.menu.isSlotBurning(i)) {
                int remainingHeight = this.menu.getBurnProgress(i, maxHeight);

                if (remainingHeight > 0) {
                    graphics.blit(
                            TEXTURE,
                            getGuiLeft() + xOffsets[i],
                            getGuiTop() + 87 + 11 - remainingHeight,
                            232,
                            12 - remainingHeight,
                            13,
                            remainingHeight
                    );
                }
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), this.titleLabelX, this.titleLabelY, 10461087, true);
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 7303023, false);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem() && !hasShiftDown()) {
            guiGraphics.renderTooltip(this.font, this.hoveredSlot.getItem(), mouseX, mouseY);
            return;
        }

        int powerSectionX = leftPos + 36;
        int powerSectionY = topPos + 70;

        if (mouseX >= powerSectionX && mouseY >= powerSectionY && mouseX <= powerSectionX + 16 && mouseY <= powerSectionY + 75) {
            int current = this.menu.getEnergy();
            int max = this.menu.getMaxEnergy();

            java.util.List<Component> tooltip = new java.util.ArrayList<>();
            tooltip.add(Component.translatable("tooltip.logicore.energy_stored", Utils.formatValues(current), Utils.formatValues(max)));

            if (hasShiftDown()) {
                int genRate = this.menu.getCurrentGenerationRate();
                tooltip.add(Component.translatable("tooltip.logicore.generating_fe_per_tick", Utils.formatValues(genRate)).withStyle(ChatFormatting.GRAY));
            }

            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        if (hasShiftDown()) {
            if (mouseX >= leftPos + 90 && mouseY >= topPos + 103 && mouseX <= leftPos + 105 && mouseY <= topPos + 118) {
                int genRate = this.menu.getCurrentGenerationRateForSlot(0);
                guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.logicore.generating_fe_per_tick", Utils.formatValues(genRate)).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
            }

            if (mouseX >= leftPos + 108 && mouseY >= topPos + 103 && mouseX <= leftPos + 123 && mouseY <= topPos + 118) {
                int genRate = this.menu.getCurrentGenerationRateForSlot(1);
                guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.logicore.generating_fe_per_tick", Utils.formatValues(genRate)).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
            }

            if (mouseX >= leftPos + 126 && mouseY >= topPos + 103 && mouseX <= leftPos + 141 && mouseY <= topPos + 118) {
                int genRate = this.menu.getCurrentGenerationRateForSlot(2);
                guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.logicore.generating_fe_per_tick", Utils.formatValues(genRate)).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
            }
            return;
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
