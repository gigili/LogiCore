package dev.gacbl.logicore.blocks.generator.ui;

import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GeneratorScreen extends MyAbstractContainerScreen<GeneratorMenu> {
    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 90;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/generator_ui.png");
        renderInventoryLabel = false;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);

        renderMainPowerBar(graphics);
        renderSidePowerBarAnimation(graphics);
        renderSideHorizontalPowerBarAnimation(graphics);
        renderFlames(graphics);
    }

    public void renderMainPowerBar(GuiGraphicsExtractor graphics) {
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

                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                        leftPos + 36, topPos + 70 + yOffset, // Screen Position (X, Y)
                        237.f, 61.f + vOffset,                   // Texture Coordinates (U, V)
                        barWidth, filledHeight,             // Size (Width, Height)
                        256, 256                            // Texture Sheet Size
                );
            }
        }
    }

    private void renderSidePowerBarAnimation(GuiGraphicsExtractor graphics) {
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

                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                        leftPos + 16, topPos + 61 + yOffset,
                        232.f, (float) vOffset,
                        barWidth, filledHeight,
                        256, 256
                );
            }
        }
    }

    private void renderSideHorizontalPowerBarAnimation(GuiGraphicsExtractor graphics) {
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
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                            leftPos + 11, topPos + 46 + 15 - filledHeightCpu,  // X, Y (Fixed position)
                            232.f, 211.f + 15 - filledHeightCpu,               // U, V (Start of texture)
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
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                            leftPos + 26, topPos + 51,  // X, Y (Fixed position)
                            237.f, 56.f,                // U, V (Start of texture)
                            filledWidth, barHeight, // Width grows, Height is fixed
                            256, 256
                    );
                }
            }
        }
    }

    private void renderFlames(GuiGraphicsExtractor graphics) {
        int maxHeight = 13;

        int[] xOffsets = {91, 110, 127};

        for (int i = 0; i < 3; i++) {
            if (this.menu.isSlotBurning(i)) {
                int remainingHeight = this.menu.getBurnProgress(i, maxHeight);

                if (remainingHeight > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                            getGuiLeft() + xOffsets[i],
                            getGuiTop() + 87 + 11 - remainingHeight,
                            232.f,
                            12.f - remainingHeight,
                            13,
                            remainingHeight,
                            256,
                            256
                    );
                }
            }
        }
    }

    // TODO: Re-enable custom tooltip rendering for the new 26.1 API
    // Tooltip rendering was reworked in MC 1.21.5. The old renderTooltip(GuiGraphics, int, int)
    // override has been replaced with a new ActiveTextCollector-based system in GuiGraphicsExtractor.
    // Custom tooltips for energy bars and generation rates need to be ported to the new API.
}
