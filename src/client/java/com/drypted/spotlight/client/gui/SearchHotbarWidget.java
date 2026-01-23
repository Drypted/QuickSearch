package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.records.RoundedCorners;
import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SearchHotbarWidget extends AbstractWidget
{
    private final int padding;
    private final boolean isRounded;
    private final int outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;

    public SearchHotbarWidget(int x, int y, int width, int height, int padding, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, height, Component.empty());
        this.padding = padding;
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        final int slots = 9;

        // width = slots * iconSize + (slots + 1) * padding
        // => iconSize = (width - (slots + 1) * padding) / slots
        final float iconSize = (this.getWidth() - this.padding * (slots + 1)) / (float) slots;

        final int endY = this.getY() + this.getHeight() + this.outlineThickness;
        final int startY = (int) Math.ceil(endY - iconSize);

        float cursor = this.getX() + this.padding;
        for (int i = 0; i < slots; i++)
        {
            RenderUtils.fillRectangle(
                    guiGraphics,
                    (int) Math.ceil(cursor),
                    startY,
                    (int) Math.ceil(cursor + iconSize),
                    endY,
                    RoundedCorners.fromSingle(this.isRounded),
                    this.outlineThickness,
                    false,
                    this.backgroundColor,
                    this.outlineColor
            );

            cursor += iconSize + this.padding;
        }
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

        private int padding = 2;
        private boolean isRounded = true;
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

        public Builder padding(int padding)
        {
            this.padding = padding;
            return this;
        }

        public Builder isRounded(boolean isRounded)
        {
            this.isRounded = isRounded;
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
                    x, y, //
                    width, height, padding, isRounded, outlineThickness, backgroundColor, outlineColor
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
