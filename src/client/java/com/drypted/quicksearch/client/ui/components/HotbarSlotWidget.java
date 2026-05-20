package com.drypted.quicksearch.client.ui.components;

import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Color;
import com.drypted.quicksearch.client.core.blueprints.ui.common.MouseButtonClick;
import com.drypted.quicksearch.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.quicksearch.client.ui.renderer.RenderCommon;
import com.drypted.quicksearch.client.ui.styling.Styles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class HotbarSlotWidget extends AbstractWidget
{
    private final int hotbarIndex;
    private final String hotbarKey;
    private final int iconPadding;
    private final RoundedCorners roundedCorners;
    private final float outlineThickness;
    private final float hotbarKeyTextScale;
    private final Color backgroundColor;
    private final Color textColor;
    private final Color outlineColor;
    private final Color focusedColor;
    private final Color highlightedColor;
    private final Color clickedColor;

    private Consumer<MouseButtonClick> onClickCallback;

    private ItemsResultData itemsResultData = ItemsResultData.EMPTY;
    private boolean showBind = false;
    private boolean highlighted = false;

    private boolean pressed = false;

    public HotbarSlotWidget(int hotbarIndex, int x, int y, int width, int height)
    {
        super(x, y, width, height, Component.empty());
        this.hotbarIndex = hotbarIndex;
        this.iconPadding = Styles.Hotbar.ICON_PADDING;
        this.roundedCorners = Styles.Hotbar.ROUNDED;
        this.outlineThickness = Styles.Hotbar.OUTLINE_THICKNESS;
        this.hotbarKeyTextScale = Styles.Hotbar.TEXT_SCALE;
        this.backgroundColor = Styles.Hotbar.TOOLTIP_BACKGROUND_COLOR;
        this.textColor = Styles.Hotbar.HELP_TEXT_COLOR;
        this.outlineColor = Styles.Hotbar.TOOLTIP_OUTLINE_COLOR;
        this.focusedColor = Styles.Hotbar.FOCUSED_COLOR;
        this.highlightedColor = Styles.Hotbar.SLOT_HIGHLIGHTED_COLOR;
        this.clickedColor = Styles.Hotbar.SLOT_PRESSED_COLOR;
        this.onClickCallback = (mouseButtonClick) -> {
        };

        this.hotbarKey = Minecraft.getInstance().options.keyHotbarSlots[hotbarIndex].getTranslatedKeyMessage()
                .getString()
                .toUpperCase();
    }

    @Override
    protected void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        RenderCommon.drawRectangle(
                guiGraphics,
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                this.roundedCorners,
                this.outlineThickness,
                true,
                this.backgroundColor,
                this.pressed
                ? this.clickedColor
                : this.highlighted
                  ? this.highlightedColor
                  : this.shouldShowBind() ? this.focusedColor : this.outlineColor
        );

        // show icon if search result data is available
        if (itemsResultData != null && !itemsResultData.isEmpty())
        {
            int iconSize = this.getHeight() - 2 * iconPadding;
            RenderCommon.drawScaledItemSize(
                    guiGraphics,
                    itemsResultData.getIcon(),
                    this.getX() + iconPadding,
                    this.getY() + iconPadding,
                    iconSize
            );
        }

        // show hotbar keybind if focused
        if (this.shouldShowBind())
        {
            final int padding = 4;

            RenderCommon.drawLabelWithScale(
                    guiGraphics,
                    hotbarKey,
                    hotbarKeyTextScale,
                    this.getX() + padding,
                    this.getY() - this.getWidth() + (padding * 2),
                    this.getRight() - padding,
                    this.getY(),
                    RoundedCorners.fromVerticalSides(true, false),
                    this.outlineThickness,
                    this.pressed
                    ? this.clickedColor
                    : this.highlighted ? highlightedColor : focusedColor,
                    textColor
            );
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick)
    {
        if (isMouseOver(event.x(), event.y()))
        {
            if (onClickCallback != null)
            {
                onClickCallback.accept(MouseButtonClick.from(event));
            }
            return true;
        }
        return false;
    }

    /* Getters and Setters */

    public ItemsResultData getSearchResultData()
    {
        return itemsResultData;
    }

    public void setSearchResultData(ItemsResultData itemsResultData)
    {
        this.itemsResultData = itemsResultData;
    }

    public void onClick(Consumer<MouseButtonClick> onClickCallback)
    {
        this.onClickCallback = onClickCallback;
    }

    public boolean shouldShowBind()
    {
        return showBind;
    }

    public void setShowBind(boolean showBind)
    {
        this.showBind = showBind;
    }

    public boolean isPressed()
    {
        return pressed;
    }

    public void setPressed(boolean pressed)
    {
        this.pressed = pressed;
    }

    public boolean isHighlighted()
    {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    public int getHotbarIndex()
    {
        return hotbarIndex;
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput)
    {
    }
}
