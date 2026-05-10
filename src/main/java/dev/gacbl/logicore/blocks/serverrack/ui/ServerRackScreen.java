package dev.gacbl.logicore.blocks.serverrack.ui;

import dev.gacbl.logicore.Config;
import dev.gacbl.logicore.blocks.computer.ComputerBlockEntity;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ServerRackScreen extends MyAbstractContainerScreen<ServerRackMenu> {
    public ServerRackScreen(ServerRackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = leftPos + 83;
        this.titleLabelY = topPos + 14;
        setTexture("textures/gui/server_rack_gui.png");
        renderInventoryLabel = false;

        if (menu.getBlockEntity() instanceof ComputerBlockEntity) {
            this.titleLabelX = 93;
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        renderMainPowerBar(graphics);
        renderSidePowerBarAnimation(graphics);
        renderSideHorizontalPowerBarAnimation(graphics);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        int powerSectionX = leftPos + 36;
        int powerSectionY = topPos + 70;

        if (mouseX >= powerSectionX && mouseY >= powerSectionY && mouseX <= powerSectionX + 16 && mouseY <= powerSectionY + 56) {
            int current = this.menu.getEnergy();
            int max = this.menu.getMaxEnergy();
            graphics.setComponentTooltipForNextFrame(this.font, List.of(Component.translatable("tooltip.logicore.energy_stored", Utils.formatValues(current), Utils.formatValues(max))), mouseX, mouseY, ItemStack.EMPTY);
        }

        if (Config.RENDER_MACHINE_INFORMATION_IN_UI.get()) {
            renderDescription(graphics);
        }
    }

    private void renderMainPowerBar(GuiGraphicsExtractor graphics) {
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

                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                        leftPos + 36, topPos + 70 + yOffset,
                        232.0f, (float) vOffset,
                        barWidth, filledHeight,
                        256, 256
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
                        232.0f, (float) vOffset,
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
                            leftPos + 11, topPos + 46 + 15 - filledHeightCpu,
                            232.0f, 211.0f + 15 - filledHeightCpu,
                            cpuSize, filledHeightCpu,
                            256, 256
                    );
                }
            }

            if (rawRatio > 0.97f) {
                float scaledRatio = (rawRatio - 0.97f) / 0.02f;
                scaledRatio = Math.min(scaledRatio, 1.0f);

                int filledWidth = (int) (scaledRatio * barWidth);

                if (filledWidth > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                            leftPos + 26, topPos + 51,
                            237.0f, 56.0f,
                            filledWidth, barHeight,
                            256, 256
                    );
                }
            }
        }
    }

    private void renderDescription(@NotNull GuiGraphicsExtractor graphics) {
        int labelSectionX = leftPos + 60;
        int labelSectionY = topPos + 45;
        int textColor = 0xFF9F9F9F;

        int cycles = this.menu.getCycles();
        int maxCycles = this.menu.getMaxCycles();

        boolean isComputer = menu.getBlockEntity() instanceof ComputerBlockEntity;

        graphics.text(this.font, Component.translatable("ui.tooltip.logicore.cycles").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX, labelSectionY, textColor);
        graphics.text(this.font, Component.translatable("ui.tooltip.logicore.base_cycles_generation").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor);
        labelSectionY += 12;

        graphics.text(this.font, Component.translatable("ui.tooltip.logicore.cycles_storage", Utils.formatValues(cycles), Utils.formatValues(maxCycles)), labelSectionX, labelSectionY, textColor);
        graphics.text(this.font, Component.literal((String.valueOf(this.menu.getBaseCycleGeneration()))), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor);
        labelSectionY += 18;

        graphics.text(this.font, Component.translatable("ui.tooltip.logicore.cycles_modifier").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX, labelSectionY, textColor);
        graphics.text(this.font, Component.translatable("ui.tooltip.logicore.cycles_produced").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor);
        labelSectionY += 12;

        int cycleModifier = (this.menu.getCyclesPerProcessor() * this.menu.getProcessorCount());
        if (this.menu.hasDataCenterBoost()) {
            cycleModifier += this.menu.getDataCenterBoost();
        }
        int cyclesToGenerate = cycleModifier + this.menu.getBaseCycleGeneration();
        if (this.menu.getProcessorCount() == 0) {
            cyclesToGenerate = 0;
        }
        graphics.text(this.font, Component.literal(String.valueOf(cycleModifier)), labelSectionX, labelSectionY, textColor);
        graphics.text(this.font, Component.literal(String.valueOf(cyclesToGenerate)), labelSectionX + (isComputer ? 95 : 85), labelSectionY, textColor);
        labelSectionY += 18;

        graphics.text(this.font, Component.translatable("ui.tooltip.logicore.fe_per_tick").plainCopy().withStyle(ChatFormatting.BOLD), labelSectionX, labelSectionY, textColor);
        labelSectionY += 12;
        if (this.menu.hasDataCenterBoost() && cyclesToGenerate > 0) {
            cyclesToGenerate -= this.menu.getDataCenterBoost();
        }
        graphics.text(this.font, Component.literal(Utils.formatValues(Math.min(cyclesToGenerate * this.menu.getFePerCycle(), 100_000))), labelSectionX, labelSectionY, textColor);
    }
}
