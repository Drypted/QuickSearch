package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ButtonWidget extends AbstractWidget
{
    private RoundedCorners rounded;
    private Color backgroundColor;

    private Runnable onClickCallback;

    public ButtonWidget(int x, int y, int width, int height, RoundedCorners rounded, Color backgroundColor)
    {
        super(x, y, width, height, Component.empty());
        this.rounded = rounded;
        this.backgroundColor = backgroundColor;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        RenderUtils.drawRectangle(
                guiGraphics,
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                this.rounded,
                0,
                false,
                this.backgroundColor,
                Colors.CLEAR
        );
    }

    /* Click */

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (this.isMouseOver(mouseX, mouseY) && this.onClickCallback != null)
        {
            this.onClickCallback.run();
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /* GETTERS & SETTERS */

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public RoundedCorners getRounded()
    {
        return rounded;
    }

    public void setRounded(RoundedCorners rounded)
    {
        this.rounded = rounded;
    }

    public void setOnClick(Runnable onClickCallback)
    {
        this.onClickCallback = onClickCallback;
    }

    /* BUILDER */

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

        private Color backgroundColor = Colors.GRAY;
        private RoundedCorners rounded = RoundedCorners.all();

        public Builder(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder backgroundColor(Color backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder rounded(RoundedCorners rounded)
        {
            this.rounded = rounded;
            return this;
        }

        public ButtonWidget build()
        {
            return new ButtonWidget(x, y, width, height, rounded, backgroundColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {

    }
}
