package com.drypted.spotlight.client.gui;

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
    private final int iconPadding;
    private final RoundedCorners roundedCorners;
    private final int outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;

    private Consumer<MouseButtonClick> onClickCallback;

    private SearchResultData searchResultData = SearchResultData.EMPTY;

    public SearchHotbarWidget(int hotbarIndex, int x, int y, int width, int height, int iconPadding, RoundedCorners roundedCorners, int outlineThickness, Color backgroundColor, Color outlineColor, Consumer<MouseButtonClick> onClickCallback)
    {
        super(x, y, width, height, Component.empty());
        this.hotbarIndex = hotbarIndex;
        this.iconPadding = iconPadding;
        this.roundedCorners = roundedCorners;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
        this.onClickCallback = onClickCallback;
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
                this.outlineColor
        );

        // show icon if search result data is available
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

        // show hotbar keybind if focused
        if (this.isFocused())
        {
            final int padding = 8;
            String hotbarKey = Minecraft.getInstance().options.keyHotbarSlots[hotbarIndex].getTranslatedKeyMessage()
                                                                                          .getString();
            int textWidth = Minecraft.getInstance().font.width(hotbarKey);

            RenderUtils.drawText(
                    guiGraphics,
                    hotbarKey.toUpperCase(),
                    this.getX() + (this.getWidth() - textWidth) / 2,
                    this.getY() - Minecraft.getInstance().font.lineHeight - 4 - padding
                    // 4 is padding of bg
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

    public Consumer<MouseButtonClick> getOnClickCallback()
    {
        return onClickCallback;
    }

    public void setOnClickCallback(Consumer<MouseButtonClick> onClickCallback)
    {
        this.onClickCallback = onClickCallback;
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
        private Color backgroundColor = Colors.BLACK.withHalfAlpha();
        private Color outlineColor = Colors.WHITE;

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
                    backgroundColor,
                    outlineColor,
                    onClickCallback
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
