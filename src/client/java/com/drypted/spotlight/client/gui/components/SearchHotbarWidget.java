package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.core.models.SearchResultData;
import com.drypted.spotlight.client.gui.models.MouseButtonClick;
import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SearchHotbarWidget extends AbstractWidget
{
    private final int hotbarIndex;
    private final String hotbarKey;
    private final int iconPadding;
    private final RoundedCorners roundedCorners;
    private final int outlineThickness;
    private final float hotbarKeyTextScale;
    private final Color backgroundColor;
    private final Color textColor;
    private final Color outlineColor;
    private final Color focusedOutlineColor;
    private final Color highlightedOutlineColor;

    private Consumer<MouseButtonClick> onClickCallback;

    private SearchResultData searchResultData = SearchResultData.EMPTY;
    private boolean showBind = false;
    private boolean highlighted = false;

    public SearchHotbarWidget(int hotbarIndex, int x, int y, int width, int height, int iconPadding, RoundedCorners roundedCorners, int outlineThickness, float hotbarKeyTextScale, Color backgroundColor, Color textColor, Color focusedOutlineColor, Color outlineColor, Color highlightedOutlineColor, Consumer<MouseButtonClick> onClickCallback)
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
        this.focusedOutlineColor = focusedOutlineColor;
        this.highlightedOutlineColor = highlightedOutlineColor;
        this.onClickCallback = onClickCallback;

        this.hotbarKey = Minecraft.getInstance().options.keyHotbarSlots[hotbarIndex].getTranslatedKeyMessage()
                                                                                    .getString()
                                                                                    .toUpperCase();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        RenderUtils.drawRectangle(
                guiGraphics,
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                this.roundedCorners,
                this.outlineThickness,
                true,
                this.backgroundColor,
                this.highlighted
                ? this.highlightedOutlineColor
                : this.shouldShowBind() ? this.focusedOutlineColor : this.outlineColor
        );

        // show icon if search result data is available
        if (searchResultData != null && !searchResultData.isEmpty())
        {
            int iconSize = this.getHeight() - 2 * iconPadding;
            RenderUtils.drawScaledItemSize(
                    guiGraphics,
                    searchResultData.getIcon(),
                    this.getX() + iconPadding,
                    this.getY() + iconPadding,
                    iconSize
            );
        }

        // show hotbar keybind if focused
        if (this.shouldShowBind())
        {
            final int padding = 4;

            RenderUtils.drawLabelWithScale(
                    guiGraphics,
                    hotbarKey,
                    hotbarKeyTextScale,
                    this.getX() + padding,
                    this.getY() - this.getWidth() + (padding * 2),
                    this.getRight() - padding,
                    this.getY(),
                    RoundedCorners.fromVerticalSides(true, false),
                    this.highlighted ? highlightedOutlineColor : focusedOutlineColor,
                    textColor
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isMouseOver(mouseX, mouseY))
        {
            if (onClickCallback != null)
            {
                MouseButtonClick click = new MouseButtonClick(mouseX, mouseY, button);
                onClickCallback.accept(click);
            }
            return true;
        }
        return false;
    }

    /* Getters and Setters */

    public SearchResultData getSearchResultData()
    {
        return searchResultData;
    }

    public void setSearchResultData(SearchResultData searchResultData)
    {
        this.searchResultData = searchResultData;
    }

    public void setOnClickCallback(Consumer<MouseButtonClick> onClickCallback)
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
        private int outlineThickness = 1;
        private float hotbarTextScale = 0.8f;
        private Color backgroundColor = Colors.BLACK.withHalfAlpha();
        private Color textColor = Colors.WHITE;
        private Color unfocusedOutlineColor = Colors.WHITE;
        private Color focusedOutlineColor = Colors.HIGHLIGHT_YELLOW;
        private Color highlightedOutlineColor = Colors.INFO_BLUE;

        private Consumer<MouseButtonClick> onClickCallback = (mouseButtonClick) -> { };

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

        public Builder unfocusedOutlineColor(Color unfocusedOutlineColor)
        {
            this.unfocusedOutlineColor = unfocusedOutlineColor;
            return this;
        }

        public Builder focusedOutlineColor(Color focusedOutlineColor)
        {
            this.focusedOutlineColor = focusedOutlineColor;
            return this;
        }

        public Builder highlightedOutlineColor(Color highlightedOutlineColor)
        {
            this.highlightedOutlineColor = highlightedOutlineColor;
            return this;
        }

        public Builder onClickCallback(Consumer<MouseButtonClick> onClickCallback)
        {
            this.onClickCallback = onClickCallback;
            return this;
        }

        public SearchHotbarWidget build()
        {
            return new SearchHotbarWidget(
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
                    focusedOutlineColor,
                    unfocusedOutlineColor,
                    highlightedOutlineColor,
                    onClickCallback
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
