package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.core.models.SearchResultData;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SearchHotbarWidget extends AbstractWidget
{
    private final int iconPadding;
    private final RoundedCorners roundedCorners;
    private final int outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;

    private SearchResultData searchResultData = SearchResultData.EMPTY;

    public SearchHotbarWidget(int x, int y, int width, int height, int iconPadding, RoundedCorners roundedCorners, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, height, Component.empty());
        this.iconPadding = iconPadding;
        this.roundedCorners = roundedCorners;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        RenderUtils.fillRectangle(
                guiGraphics,
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                this.roundedCorners,
                this.outlineThickness,
                true,
                this.backgroundColor,
                this.outlineColor
        );

        if (searchResultData != null && searchResultData.isNotEmpty())
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

    /* Builder */

    public static Builder builder(int x, int y, int width, int height)
    {
        return new Builder(x, y, width, height);
    }

    public static class Builder
    {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private int iconPadding = 2;
        private RoundedCorners roundedCorners = RoundedCorners.all();
        private int outlineThickness = 1;
        private Color backgroundColor = Colors.BLACK.withHalfAlpha();
        private Color outlineColor = Colors.WHITE;

        public Builder(int x, int y, int width, int height)
        {
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

        public Builder backgroundColor(Color backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder outlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return this;
        }

        public SearchHotbarWidget build()
        {
            return new SearchHotbarWidget(
                    x,
                    y, //
                    width,
                    height,
                    iconPadding,
                    roundedCorners,
                    outlineThickness,
                    backgroundColor,
                    outlineColor
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
