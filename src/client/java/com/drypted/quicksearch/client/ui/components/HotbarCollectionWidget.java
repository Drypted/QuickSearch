package com.drypted.quicksearch.client.ui.components;

import com.drypted.quicksearch.client.QuickSearchClient;
import com.drypted.quicksearch.client.core.actions.ReplaceHotbarItemAction;
import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Color;
import com.drypted.quicksearch.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.quicksearch.client.ui.renderer.RenderCommon;
import com.drypted.quicksearch.client.ui.styling.Styles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class HotbarCollectionWidget extends AbstractWidget
{
    private static final int HOTBAR_WIDTH = 200;

    public static final int HOTBAR_SLOTS = 9;
    private static final int SLOT_SIZE = 20;
    public static final int MIN_SLOT_SPACING = 2;
    public static final int MAX_SLOT_SPACING = 12;

    private static final int HELP_TEXT_MARGIN = 16;
    private static final int HELP_TEXT_HEIGHT = 14;

    private static final int CLOSE_BUTTON_PADDING = 4;
    private static final int CLOSE_BUTTON_SIZE = HELP_TEXT_HEIGHT - (CLOSE_BUTTON_PADDING * 2) - 1;
    // (- 1) because drawX draws 1 pixel extra in vertical direction

    private static final String CLOSE_BUTTON_TOOLTIP_TEXT = "Hide instructions";
    private static final float CLOSE_BUTTON_TOOLTIP_SCALE = 0.8f;
    private static final int CLOSE_BUTTON_TOOLTIP_PADDING_X = 8;
    private static final int CLOSE_BUTTON_TOOLTIP_PADDING_Y = 3;
    private static final int CLOSE_BUTTON_TOOLTIP_OFFSET_Y = 1;
    private static final int MIN_HELP_TEXT_WIDTH = 225;

    private final ArrayList<HotbarSlotWidget> hotbarSlotWidgets = new ArrayList<>(HOTBAR_SLOTS);
    private @Nullable HotbarSlotWidget selectedHotbarWidget = null;
    private int activeMouseModifiers = 0;

    private HotbarHelpText hotbarInstructionText = HotbarHelpText.UNSELECTED;
    private boolean anySlotHighlighted = false;

    private final float outlineThickness = Styles.Hotbar.HELP_TOOLTIP_OUTLINE_THICKNESS;

    private final Color HotarInstructionHighlightedColor = Styles.Hotbar.HELP_TOOLTIP_HIGHLIGHTED_COLOR;
    private final Color HotbarInstructionColor = Styles.Hotbar.HELP_TOOLTIP_FOCUSED_COLOR;
    private final Color HotbarInstructionTextColor = Styles.Hotbar.HELP_TOOLTIP_TEXT_COLOR;
    private final Color HotbarInstructionOutlineColor = Styles.Hotbar.HELP_TOOLTIP_OUTLINE_COLOR;
    private final Color CloseButtonColor = Styles.Hotbar.HELP_TOOLTIP_CLOSE_BUTTON_COLOR;
    private final Color TooltipBackgroundColor = Styles.Hotbar.HELP_CLOSE_TOOLTIP_BACKGROUND_COLOR;
    private final Color TooltipOutlineColor = Styles.Hotbar.HELP_CLOSE_TOOLTIP_OUTLINE_COLOR;
    private final Color TooltipTextColor = Styles.Hotbar.HELP_CLOSE_TOOLTIP_TEXT_COLOR;

    public HotbarCollectionWidget(int centerX, int searchBarY, int width)
    {
        super(0, 0, 0, 0, Component.empty());
        final int startY = searchBarY - SLOT_SIZE;

        distributeHotbarSlots(centerX, width, startY);

        // setting bounds
        this.setX(centerX - (width / 2));
        this.setY(startY - HELP_TEXT_MARGIN - HELP_TEXT_HEIGHT);
        this.setWidth(width);
        this.setHeight(searchBarY - (startY - HELP_TEXT_MARGIN - HELP_TEXT_HEIGHT));
    }

    private void distributeHotbarSlots(int centerX, int width, int startY)
    {
        final int slotsWidth = SLOT_SIZE * HOTBAR_SLOTS;
        final int remainingWidth = width - slotsWidth;
        int spacing = remainingWidth / (HOTBAR_SLOTS - 1);
        spacing = Math.clamp(spacing, MIN_SLOT_SPACING, MAX_SLOT_SPACING);
        final int usedWidth = slotsWidth + (spacing * (HOTBAR_SLOTS - 1));

        int cursor = centerX - (usedWidth / 2);

        for (int i = 0; i < HOTBAR_SLOTS; i++)
        {
            // extra 1 pixel for intersection
            final HotbarSlotWidget hotbarWidget = new HotbarSlotWidget(i, cursor, startY + 1, SLOT_SIZE, SLOT_SIZE);
            hotbarWidget.setOnClickStart(mouseButtonClick -> {
                ItemsResultData item = hotbarWidget.getSearchResultData();
                if (item == null) return;

                onHotbarKeyPressed(hotbarWidget, this.activeMouseModifiers);
            });
            this.hotbarSlotWidgets.add(hotbarWidget);
            cursor += SLOT_SIZE + spacing;
        }
    }

    public static HotbarCollectionWidget create(int centerX, int searchBarY, int width)
    {
        return new HotbarCollectionWidget(centerX, searchBarY, width);
    }

    /* RENDERING */

    @Override
    protected void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        hotbarSlotWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));

        if (QuickSearchClient.getConfig().hotbar.showHotbarHelpText && this.isFocused())
        {
            int startX = this.getX();
            int endX = this.getRight();
            if (this.getWidth() < MIN_HELP_TEXT_WIDTH)
            {
                // is size is smaller, expand to minimum size
                int diff = MIN_HELP_TEXT_WIDTH - this.getWidth();
                startX -= diff / 2;
                endX += diff / 2;
            }
            Color backgroundColor = this.anySlotHighlighted ? HotarInstructionHighlightedColor : HotbarInstructionColor;
            RenderCommon.drawLabelWithScale(
                    guiGraphics,
                    hotbarInstructionText.getText(),
                    0.75f,
                    startX,
                    this.getY(),
                    endX,
                    this.getY() + HELP_TEXT_HEIGHT,
                    RoundedCorners.all(),
                    this.outlineThickness,
                    backgroundColor,
                    HotbarInstructionOutlineColor,
                    HotbarInstructionTextColor
            );

            drawCloseButton(guiGraphics, startX);

            if (this.isOverCloseButton(mouseX, mouseY))
            {
                drawCloseButtonTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    /* Close Button */

    private void drawCloseButton(GuiGraphics guiGraphics, int xPos)
    {
        final int startX = xPos + CLOSE_BUTTON_PADDING;
        final int startY = this.getY() + CLOSE_BUTTON_PADDING;
        final int endX = startX + CLOSE_BUTTON_SIZE;
        final int endY = startY + CLOSE_BUTTON_SIZE;

        RenderCommon.drawX(guiGraphics, startX, startY, endX, endY, CloseButtonColor, 1, true);
    }

    private void drawCloseButtonTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        final int zOrder = 500;

        final float scale = CLOSE_BUTTON_TOOLTIP_SCALE;

        final float textWidth = Minecraft.getInstance().font.width(CLOSE_BUTTON_TOOLTIP_TEXT) * scale;
        final float textHeight = Minecraft.getInstance().font.lineHeight * scale;

        final int posX = (int) (mouseX - (textWidth - CLOSE_BUTTON_TOOLTIP_PADDING_X) / 2.0f);
        final int posY =
                this.getY() - (int) textHeight - CLOSE_BUTTON_TOOLTIP_PADDING_Y - CLOSE_BUTTON_TOOLTIP_OFFSET_Y;

        RenderCommon.drawLabel(
                guiGraphics,
                CLOSE_BUTTON_TOOLTIP_TEXT,
                posX,
                posY,
                scale,
                CLOSE_BUTTON_TOOLTIP_PADDING_X,
                CLOSE_BUTTON_TOOLTIP_PADDING_Y,
                RoundedCorners.all(),
                TooltipBackgroundColor,
                TooltipOutlineColor,
                TooltipTextColor
        );
    }

    private boolean isOverCloseButton(double mouseX, double mouseY)
    {
        final int startX = this.getX() + CLOSE_BUTTON_PADDING;
        final int startY = this.getY() + CLOSE_BUTTON_PADDING;
        final int endX = startX + CLOSE_BUTTON_SIZE;
        final int endY = startY + CLOSE_BUTTON_SIZE + 2;
        // increase bounds by 2 pixels to match the drawn X bounds, because drawX draws 1 pixel extra in vertical direction

        return mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY;
    }

    /* HOTBAR */

    private void highlightSlot(int slotIndex)
    {
        // update state
        hotbarInstructionText = HotbarHelpText.SELECTED;
        anySlotHighlighted = true;

        // unhighlight all first
        hotbarSlotWidgets.forEach(widget -> widget.setHighlighted(false));
        // get slot index and set show bind to true for that widget
        hotbarSlotWidgets.stream()
                         .filter(widget -> widget.getHotbarIndex() == slotIndex)
                         .findFirst()
                         .ifPresent(widget -> widget.setHighlighted(true));
    }

    private void unhighlightAllSlots()
    {
        // update state
        anySlotHighlighted = false;
        hotbarInstructionText = HotbarHelpText.UNSELECTED;

        // unhighlight all
        hotbarSlotWidgets.forEach(widget -> widget.setHighlighted(false));
    }

    public void onHotbarKeyPressed(HotbarSlotWidget widget, int modifiers)
    {
        hotbarSlotWidgets.forEach(w -> w.setPressed(false));
        widget.setPressed(true);

        // if shift pressed, select hotbar slot only
        if (isShiftPressed(modifiers))
        {
            this.selectedHotbarWidget = widget;
            this.highlightSlot(selectedHotbarWidget.getHotbarIndex());
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
        {
            // if a slot is already selected, use that slot
            if (selectedHotbarWidget != null)
            {
                ReplaceHotbarItemAction.run(
                        player,
                        selectedHotbarWidget.getSearchResultData(),
                        widget.getHotbarIndex()
                );

                // used this one
                selectedHotbarWidget = null;
                this.unhighlightAllSlots();
                return;
            }

            ReplaceHotbarItemAction.run(player, widget.getSearchResultData(), widget.getHotbarIndex());
        }
    }

    /* STATICS */

    private static boolean isShiftPressed(int modifiers) { return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0; }

    /* GETTERS */

    public ArrayList<HotbarSlotWidget> getWidgets() { return hotbarSlotWidgets; }

    /* FOCUS & ONCLICK */

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        if (!focused) this.selectedHotbarWidget = null;

        hotbarSlotWidgets.forEach(w -> { //
            w.setShowBind(focused);
            if (focused) // clear prev state on focus
            {
                w.setPressed(false);
                w.setHighlighted(false);
                this.anySlotHighlighted = false;
            }
        });
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mEv, boolean doubleClick)
    {
        if (this.isOverCloseButton(mEv.x(), mEv.y()))
        {
            return true;
        }

        for (HotbarSlotWidget widget : hotbarSlotWidgets)
        {
            if (!isOverSlot(widget, mEv.x(), mEv.y())) continue;

            this.activeMouseModifiers = mEv.modifiers();
            final boolean consumed = widget.mouseClicked(mEv, doubleClick);
            this.activeMouseModifiers = 0;
            super.mouseClicked(mEv, doubleClick);
            return consumed;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mEv)
    {
        if (this.isOverCloseButton(mEv.x(), mEv.y()))
        {
            QuickSearchClient.getConfig().hotbar.showHotbarHelpText = false;
            QuickSearchClient.saveConfig();
        }

        unpressAllSlots();

        return super.mouseReleased(mEv);
    }

    private boolean isOverSlot(@NonNull HotbarSlotWidget widget, double mouseX, double mouseY)
    {
        final int startX = widget.getX();
        final int startY = widget.getY();
        final int endX = startX + widget.getWidth();
        final int endY = startY + widget.getHeight();

        return mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY;
    }

    public void unpressAllSlots()
    {
        this.hotbarSlotWidgets.forEach(widget -> widget.setPressed(false));
    }

    /* PRIVATE HELPERS */

    private enum HotbarHelpText
    {
        UNSELECTED("key/click: move to slot, shift + key/click: select slot"),
        SELECTED("key/click: move selected to slot");

        private final String text;

        HotbarHelpText(String text) { this.text = text; }

        public String getText() { return text; }
    }

    /* OVERRIDES */

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput)
    {
    }
}