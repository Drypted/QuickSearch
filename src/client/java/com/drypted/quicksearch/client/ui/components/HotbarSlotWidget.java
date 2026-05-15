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

    public HotbarSlotWidget(int hotbarIndex, int x, int y, int width, int height, int iconPadding, RoundedCorners roundedCorners, float outlineThickness, float hotbarKeyTextScale, Color backgroundColor, Color textColor, Color focusedColor, Color outlineColor, Color highlightedColor, Color clickedColor, Consumer<MouseButtonClick> onClickCallback)
    {
        super(x, y, width, height, Component.empty());
        this.hotbarIndex = hotbarIndex;
        this.iconPadding = iconPadding;
        this.roundedCorners = roundedCorners;
        this.outlineThickness = outlineThickness;
        this.hotbarKeyTextScale = hotbarKeyTextScale;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.outlineColor = outlineColor;
        this.focusedColor = focusedColor;
        this.highlightedColor = highlightedColor;
        this.clickedColor = clickedColor;
        this.onClickCallback = onClickCallback;

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

    /* Builder */

    public static Builder builder(int hotbarIndex, int x, int y, int width, int height)
    {
        return new Builder(hotbarIndex, x, y, width, height);
    }

    public static class Builder
    {
        private final int hotbarIndex;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private int iconPadding = 2;
        private RoundedCorners roundedCorners = RoundedCorners.all();
        private float outlineThickness = Styles.Hotbar.OUTLINE_THICKNESS;
        private float hotbarTextScale = 0.8f;
        private Color backgroundColor = Styles.Hotbar.TOOLTIP_BACKGROUND_COLOR;
        private Color textColor = Styles.Hotbar.HELP_TEXT_COLOR;
        private Color unfocusedColor = Styles.Hotbar.TOOLTIP_OUTLINE_COLOR;
        private Color focusedColor = Styles.Hotbar.FOCUSED_COLOR;
        private Color highlightedColor = Styles.Hotbar.SLOT_HIGHLIGHTED_COLOR;
        private Color clickedColor = Styles.Hotbar.SLOT_PRESSED_COLOR;

        private Consumer<MouseButtonClick> onClickCallback = (mouseButtonClick) -> {
        };

        public Builder(int hotbarIndex, int x, int y, int width, int height)
        {
            this.hotbarIndex = hotbarIndex;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder iconPadding(int iconPadding)
        {
            this.iconPadding = iconPadding;
            return this;
        }

        public Builder isRounded(RoundedCorners roundedCorners)
        {
            this.roundedCorners = roundedCorners;
            return this;
        }

        public Builder outlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return this;
        }

        public Builder hotbarTextScale(float hotbarTextScale)
        {
            this.hotbarTextScale = hotbarTextScale;
            return this;
        }

        public Builder backgroundColor(Color backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder textColor(Color textColor)
        {
            this.textColor = textColor;
            return this;
        }

        public Builder unfocusedColor(Color unfocusedColor)
        {
            this.unfocusedColor = unfocusedColor;
            return this;
        }

        public Builder focusedColor(Color focusedColor)
        {
            this.focusedColor = focusedColor;
            return this;
        }

        public Builder highlightedColor(Color highlightedColor)
        {
            this.highlightedColor = highlightedColor;
            return this;
        }

        public Builder clickedColor(Color clickedColor)
        {
            this.clickedColor = clickedColor;
            return this;
        }

        public Builder onClickCallback(Consumer<MouseButtonClick> onClickCallback)
        {
            this.onClickCallback = onClickCallback;
            return this;
        }

        public HotbarSlotWidget build()
        {
            return new HotbarSlotWidget(
                    hotbarIndex,
                    x,
                    y, //
                    width,
                    height,
                    iconPadding,
                    roundedCorners,
                    outlineThickness,
                    hotbarTextScale,
                    backgroundColor,
                    textColor,
                    focusedColor,
                    unfocusedColor,
                    highlightedColor,
                    clickedColor,
                    onClickCallback
            );
        }
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput)
    {
    }
}
