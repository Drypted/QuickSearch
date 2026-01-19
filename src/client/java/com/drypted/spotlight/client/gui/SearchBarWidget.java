package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SearchBarWidget extends AbstractWidget
{
    private final boolean isRounded;
    private final int outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;

    public SearchBarWidget(int x, int y, int width, int height, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, height, Component.empty());
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f)
    {
        RenderUtils.fillRectangle(
                guiGraphics,
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                this.isRounded,
                this.outlineThickness,
                true,
                this.backgroundColor,
                this.outlineColor
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {

    }

    /* ------------------------ Builder Class ------------------------ */

    public static Builder builder(int x, int y, int width, int height)
    {
        return new Builder(x, y, width, height);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private boolean isRounded = false;
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

        public Builder setRounded(boolean isRounded)
        {
            this.isRounded = isRounded;
            return this;
        }

        public Builder setOutlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return this;
        }

        public Builder setBackgroundColor(Color backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setOutlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return this;
        }

        public SearchBarWidget build()
        {
            return new SearchBarWidget(x, y, width, height, isRounded, outlineThickness, backgroundColor, outlineColor);
        }
    }
}
