package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.actions.Actions;
import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import com.drypted.spotlight.client.models.SearchResultData;
import com.drypted.spotlight.client.styling.Styles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class HotbarCollectionWidget extends AbstractWidget
{
    public static final int HOTBAR_SLOT_PADDING = 2;
    public static final int HOTBAR_SLOTS = 9;

    private static final int HELP_TEXT_MARGIN = 16;
    private static final int HELP_TEXT_HEIGHT = 12;

    private static final int CLOSE_BUTTON_PADDING = 3;
    private static final int CLOSE_BUTTON_SIZE = HELP_TEXT_HEIGHT - (CLOSE_BUTTON_PADDING * 2) - 1;
    // (- 1) because drawX draws 1 pixel extra in vertical direction

    private static final String CLOSE_BUTTON_TOOLTIP_TEXT = "Hide instructions";
    private static final float CLOSE_BUTTON_TOOLTIP_SCALE = 0.8f;
    private static final int CLOSE_BUTTON_TOOLTIP_PADDING_X = 8;
    private static final int CLOSE_BUTTON_TOOLTIP_PADDING_Y = 3;
    private static final int CLOSE_BUTTON_TOOLTIP_OFFSET_Y = 1;

    private final ArrayList<HotbarSlotWidget> hotbarSlotWidgets = new ArrayList<>(HOTBAR_SLOTS);
    private @Nullable HotbarSlotWidget selectedHotbarWidget = null;

    private HotbarHelpText hotbarInstructionText = HotbarHelpText.UNSELECTED;
    private boolean anySlotHighlighted = false;

    private final Color SlotHighlightedColor = Styles.Hotbar.SLOT_HIGHLIGHTED_COLOR;
    private final Color SlotFocusedColor = Styles.Hotbar.FOCUSED_COLOR;
    private final Color HotbarInstructionTextColor = Styles.Hotbar.HELP_TEXT_COLOR;
    private final Color CloseButtonColor = Styles.Hotbar.HELP_TEXT_CLOSE_BUTTON_COLOR;
    private final Color TooltipBackgroundColor = Styles.Hotbar.TOOLTIP_BACKGROUND_COLOR;
    private final Color TooltipOutlineColor = Styles.Hotbar.TOOLTIP_OUTLINE_COLOR;
    private final Color TooltipTextColor = Styles.Hotbar.TOOLTIP_TEXT_COLOR;

    public HotbarCollectionWidget(int startX, int width, int endY)
    {
        super(0, 0, 0, 0, Component.empty());
        final float iconSize = (width - HOTBAR_SLOT_PADDING * (HOTBAR_SLOTS + 1)) / (float) HOTBAR_SLOTS;
        final int startY = (int) Math.ceil(endY - iconSize);
        float cursor = startX + HOTBAR_SLOT_PADDING;

        for (int i = 0; i < HOTBAR_SLOTS; i++)
        {
            final HotbarSlotWidget hotbarWidget = HotbarSlotWidget.builder(
                    i, //
                    (int) Math.ceil(cursor),
                    startY,
                    (int) Math.ceil(iconSize),
                    (int) Math.ceil(iconSize)
            ).build();
            hotbarWidget.onClick(mouseButtonClick -> {
                SearchResultData item = hotbarWidget.getSearchResultData();
                if (item == null || item.isEmpty())
                    return;

                onHotbarKeyPressed(hotbarWidget, 0);
            });
            this.hotbarSlotWidgets.add(hotbarWidget);
            cursor += iconSize + HOTBAR_SLOT_PADDING;
        }

        // settings bounds
        this.setX(startX);
        this.setY(startY - HELP_TEXT_MARGIN - HELP_TEXT_HEIGHT);
        this.setWidth(width);
        this.setHeight(endY - (startY - HELP_TEXT_MARGIN - HELP_TEXT_HEIGHT));
    }

    public static HotbarCollectionWidget create(int startX, int width, int endY)
    {
        return new HotbarCollectionWidget(startX, width, endY);
    }

    /* RENDERING */

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        hotbarSlotWidgets.forEach(widget -> widget.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        ));

        if (SpotlightEntryClient.getConfig().showHotbarHelpText && this.isFocused())
        {
            RenderUtils.drawLabelWithScale(
                    guiGraphics,
                    hotbarInstructionText.getText(),
                    0.75f,
                    this.getX(),
                    this.getY(),
                    this.getRight(),
                    this.getY() + HELP_TEXT_HEIGHT,
                    RoundedCorners.all(),
                    this.anySlotHighlighted ? SlotHighlightedColor : SlotFocusedColor,
                    HotbarInstructionTextColor
            );

            drawCloseButton(guiGraphics);

            if (this.isOverCloseButton(mouseX, mouseY))
            {
                drawCloseButtonTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    /* Close Button */

    private void drawCloseButton(GuiGraphics guiGraphics)
    {
        final int startX = this.getX() + CLOSE_BUTTON_PADDING;
        final int startY = this.getY() + CLOSE_BUTTON_PADDING;
        final int endX = startX + CLOSE_BUTTON_SIZE;
        final int endY = startY + CLOSE_BUTTON_SIZE;

        RenderUtils.drawX(guiGraphics, startX, startY, endX, endY, CloseButtonColor, 1, true);
    }

    private void drawCloseButtonTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        final int zOrder = 500;

        final float scale = CLOSE_BUTTON_TOOLTIP_SCALE;

        final float textWidth = Minecraft.getInstance().font.width(CLOSE_BUTTON_TOOLTIP_TEXT) * scale;
        final float textHeight = Minecraft.getInstance().font.lineHeight * scale;

        final int posX = (int) (mouseX - (textWidth - CLOSE_BUTTON_TOOLTIP_PADDING_X) / 2.0f);
        final int posY = this.getY() - (int) textHeight - CLOSE_BUTTON_TOOLTIP_PADDING_Y - CLOSE_BUTTON_TOOLTIP_OFFSET_Y;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, zOrder);

        RenderUtils.drawLabel(
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

        guiGraphics.pose().popPose();
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
        hotbarSlotWidgets.stream().filter(widget -> widget.getHotbarIndex() == slotIndex)
                         .findFirst().ifPresent(widget -> widget.setHighlighted(true));
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
        widget.setPressed(true);

        // if shift pressed, select hotbar slot only
        if (isModifierPressed(modifiers, GLFW.GLFW_MOD_SHIFT))
        {
            this.selectedHotbarWidget = widget;
            this.highlightSlot(selectedHotbarWidget.getHotbarIndex());
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
        {
            // if there is item in the selected hotbar slot
            SearchResultData item = widget.getSearchResultData();

            // if a slot is already selected, use that slot
            if (selectedHotbarWidget != null)
            {
                if (selectedHotbarWidget.getSearchResultData() != null)
                    Actions.replaceHotbarItem(
                            player,
                            selectedHotbarWidget.getSearchResultData(),
                            widget.getHotbarIndex()
                    );

                // used this one
                selectedHotbarWidget = null;
                this.unhighlightAllSlots();
            }
            else if (item != null)
            {
                Actions.replaceHotbarItem(player, item, widget.getHotbarIndex());
            }
        }
    }

    public void onAnyKeyReleased()
    {
        this.hotbarSlotWidgets.forEach(widget -> widget.setPressed(false));
    }

    /* STATICS */

    private static boolean isModifierPressed(int modifiers, int modifierToCheck)
    {
        return (modifiers & modifierToCheck) != 0;
    }

    /* GETTERS */

    public ArrayList<HotbarSlotWidget> getWidgets()
    {
        return hotbarSlotWidgets;
    }

    /* FOCUS & ONCLICK */

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        if (!focused)
            this.selectedHotbarWidget = null;

        hotbarSlotWidgets.forEach(w -> w.setShowBind(focused));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (this.isOverCloseButton(mouseX, mouseY))
        {
            SpotlightEntryClient.getConfig().showHotbarHelpText = false;
            SpotlightEntryClient.saveConfig();
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /* PRIVATE HELPERS */

    private enum HotbarHelpText
    {
        UNSELECTED("key: move to slot, shift + key: select slot"),
        SELECTED("key: move selected to slot");

        private final String text;

        HotbarHelpText(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }
    }

    /* OVERRIDES */

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
}