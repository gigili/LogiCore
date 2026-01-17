package dev.gacbl.logicore.blocks.serverrack.ui;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.computer.ComputerBlockEntity;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ServerRackScreen extends MyAbstractContainerScreen<ServerRackMenu> {
    public ServerRackScreen(ServerRackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 80;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/server_rack_gui.png");
        renderInventoryLabel = false;

        if (menu.getBlockEntity() instanceof ComputerBlockEntity) {
            this.titleLabelX = 87;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        renderMainPowerBar(graphics);
        renderSidePowerBarAnimation(graphics);
        renderSideHorizontalPowerBarAnimation(graphics);

        if (Config.RENDER_MACHINE_INFORMATION_IN_UI.get()) {
            renderDescription(graphics);
        }
    }

    private void renderMainPowerBar(GuiGraphics graphics) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();

        if (maxEnergy > 0 && energy > 0) {
            int barHeight = 56;
            int barWidth = 16;
            int textureTotalHeight = 56;

            int filledHeight = (int) (((float) energy / maxEnergy) * barHeight);

            if (filledHeight > 0) {
                int yOffset = barHeight - filledHeight;
                int vOffset = textureTotalHeight - filledHeight;

                graphics.blit(TEXTURE,
                        leftPos + 36, topPos + 70 + yOffset, // Screen Position (X, Y)
                        232, vOffset,                       // Texture Coordinates (U, V)
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

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int powerSectionX = leftPos + 36;
        int powerSectionY = topPos + 70;

        if (mouseX >= powerSectionX && mouseY >= powerSectionY && mouseX <= powerSectionX + 16 && mouseY <= powerSectionY + 56) {
            int current = this.menu.getEnergy();
            int max = this.menu.getMaxEnergy();
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.logicore.energy_stored", Utils.formatValues(current), Utils.formatValues(max)), mouseX, mouseY);
        }
    }

    private void renderDescription(@NotNull GuiGraphics graphics) {
        int labelSectionX = leftPos + 60;
        int labelSectionY = topPos + 45;
        int textColor = 10461087;

        int cycles = this.menu.getCycles();
        int maxCycles = this.menu.getMaxCycles();

        boolean isComputer = menu.getBlockEntity() instanceof ComputerBlockEntity;

        graphics.drawString(this.font, Component.translatable("ui.tooltip.logicore.cycles").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX, labelSectionY, textColor, false);
        graphics.drawString(this.font, Component.translatable("ui.tooltip.logicore.base_cycles_generation").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor, false);
        labelSectionY += 12;

        graphics.drawString(this.font, Component.translatable("ui.tooltip.logicore.cycles_storage", Utils.formatValues(cycles), Utils.formatValues(maxCycles)), labelSectionX, labelSectionY, textColor, false);
        graphics.drawString(this.font, Component.literal((String.valueOf(this.menu.getBaseCycleGeneration()))), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor, false);
        labelSectionY += 18;

        graphics.drawString(this.font, Component.translatable("ui.tooltip.logicore.cycles_modifier").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX, labelSectionY, textColor, false);
        graphics.drawString(this.font, Component.translatable("ui.tooltip.logicore.cycles_produced").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor, false);
        labelSectionY += 12;

        int cycleModifier = (this.menu.getCyclesPerProcessor() * this.menu.getProcessorCount());
        if (this.menu.hasDataCenterBoost()) {
            cycleModifier += this.menu.getDataCenterBoost();
        }
        int cyclesToGenerate = cycleModifier + this.menu.getBaseCycleGeneration();
        if (this.menu.getProcessorCount() == 0) {
            cyclesToGenerate = 0;
        }
        graphics.drawString(this.font, Component.literal(String.valueOf(cycleModifier)), labelSectionX, labelSectionY, textColor, false);
        graphics.drawString(this.font, Component.literal(String.valueOf(cyclesToGenerate)), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor, false);
        labelSectionY += 18;

        graphics.drawString(this.font, Component.translatable("ui.tooltip.logicore.fe_per_tick").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX, labelSectionY, textColor, false);
        labelSectionY += 12;
        if (this.menu.hasDataCenterBoost() && cyclesToGenerate > 0) {
            cyclesToGenerate -= this.menu.getDataCenterBoost();
        }
        graphics.drawString(this.font, Component.literal(String.valueOf(cyclesToGenerate * this.menu.getFePerCycle())), labelSectionX, labelSectionY, textColor, false);
    }
}
