package dev.gacbl.logicore.client.ui;

import dev.gacbl.logicore.client.ClientKnowledgeData;
import dev.gacbl.logicore.core.Utils;
import dev.gacbl.logicore.core.ui.MyAbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class KnowledgeScreen extends MyAbstractContainerScreen<KnowledgeMenu> {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 9;
    private static final int SLOT_SIZE = 16;
    private static final int SLOT_SPACING = 18;
    private static final int GRID_LEFT = 26;
    private static final int GRID_TOP = 64;
    private static final int SCROLL_BAR_LEFT = 198;
    private static final int SCROLL_BAR_TOP = 64;
    private static final int SCROLL_BAR_TRACK_HEIGHT = 161;
    private static final int SCROLL_BAR_THUMB_WIDTH = 10;
    private static final int SCROLL_BAR_THUMB_HEIGHT = 13;
    private static final int SCROLL_BAR_TEX_X = 232;
    private static final int SCROLL_BAR_TEX_Y = 0;

    private final List<ItemStack> items = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;

    public KnowledgeScreen(KnowledgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        setTexture("textures/gui/knowledge_ui.png");
        renderInventoryLabel = false;
        this.titleLabelX = leftPos + 70;
        this.titleLabelY = topPos + 14;
    }

    @Override
    protected void init() {
        super.init();

        items.clear();
        Set<String> unlockedItems = ClientKnowledgeData.getUnlockedItems();
        for (String itemKey : unlockedItems) {
            ItemStack stack = Utils.getItemStackFromKey(itemKey);
            if (stack != null && !stack.isEmpty()) {
                items.add(stack);
            }
        }
        items.sort(Comparator.comparing(s -> s.getHoverName().getString()));

        int totalSlots = GRID_COLS * GRID_ROWS;
        maxScrollOffset = Math.max(0, (int) Math.ceil((items.size() - totalSlots) / (double) GRID_COLS));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        int startIndex = scrollOffset * GRID_COLS;
        int totalVisible = GRID_COLS * GRID_ROWS;

        for (int i = 0; i < totalVisible; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int itemIndex = startIndex + i;
            if (itemIndex >= items.size()) break;

            int slotX = leftPos + GRID_LEFT + col * SLOT_SPACING;
            int slotY = topPos + GRID_TOP + row * SLOT_SPACING;
            graphics.renderItem(items.get(itemIndex), slotX + 1, slotY + 1);
        }

        if (maxScrollOffset > 0) {
            int trackHeight = SCROLL_BAR_TRACK_HEIGHT - SCROLL_BAR_THUMB_HEIGHT;
            int thumbY = topPos + SCROLL_BAR_TOP + (scrollOffset * trackHeight) / maxScrollOffset;
            graphics.blit(TEXTURE, leftPos + SCROLL_BAR_LEFT, thumbY,
                    SCROLL_BAR_TEX_X, SCROLL_BAR_TEX_Y,
                    SCROLL_BAR_THUMB_WIDTH, SCROLL_BAR_THUMB_HEIGHT, 256, 256);
        }

        ItemStack hoveredStack = getHoveredItem(mouseX, mouseY);
        if (hoveredStack != null) {
            graphics.renderTooltip(font, hoveredStack, mouseX, mouseY);
        }
    }

    private ItemStack getHoveredItem(int mouseX, int mouseY) {
        int startIndex = scrollOffset * GRID_COLS;
        for (int i = 0; i < GRID_COLS * GRID_ROWS; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int slotX = leftPos + GRID_LEFT + col * SLOT_SPACING;
            int slotY = topPos + GRID_TOP + row * SLOT_SPACING;
            int itemIndex = startIndex + i;
            if (itemIndex < items.size()
                    && mouseX >= slotX && mouseX < slotX + SLOT_SIZE
                    && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                return items.get(itemIndex);
            }
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (maxScrollOffset > 0) {
            int prevScroll = scrollOffset;
            if (deltaY < 0) {
                scrollOffset = Math.min(scrollOffset + 1, maxScrollOffset);
            } else {
                scrollOffset = Math.max(scrollOffset - 1, 0);
            }
            return prevScroll != scrollOffset;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
