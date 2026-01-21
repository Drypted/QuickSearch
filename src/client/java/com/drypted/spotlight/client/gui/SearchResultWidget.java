package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;

public class SearchResultWidget extends AbstractWidget
{
    private static final Font FONT = Minecraft.getInstance().font;

    private String text;
    private final int padding;
    private final boolean isRounded;
    private final int outlineThickness;
    private Color backgroundColor;
    private Color textColor;
    private Color hoverColor;
    private Color clickColor;
    private boolean isToggleButton;
    private boolean isTextCentered;
    private boolean pressed;
    private boolean highlighted;

    private Color outlineColor = Colors.CLEAR;
    private float scale;

    // callback
    private BiConsumer<MouseButtonClick, Boolean> onClickCallback = (e, pressed) -> {
    };

    public SearchResultWidget(int x, int y, int padding, boolean isRounded, String text, int outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor, float scale)
    {
        super(x, y, 0, 0, Component.empty());
        this.text = text;
        this.padding = padding;
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;
        this.scale = scale;

        int textW = FONT.width(text);
        int textH = FONT.lineHeight;

        this.width = textW + (padding * 2);
        this.height = textH + (padding * 2);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta)
    {
        textColor.makeOpaque();

        // getX/Y = x/y top left corner pos
        final int startPosX = getX();
        final int startPosY = getY();
        final int endPosX = startPosX + width;
        final int endPosY = startPosY + height;

        Color outlineColor;
        if (this.getOutlineColor() != Colors.CLEAR) outlineColor = this.getOutlineColor();
        else if (this.pressed) outlineColor = clickColor;
        else outlineColor = this.isHovered ? hoverColor : backgroundColor;

        boolean renderOutline = this.isHovered() || this.isPressed() || this.isHighlighted();

        RenderUtils.fillRectangle(
                g,
                startPosX,
                startPosY,
                endPosX,
                endPosY,
                this.isRounded,
                this.outlineThickness,
                renderOutline,
                this.backgroundColor,
                outlineColor
        );

        int textX;
        int textY;

        if (isTextCentered)
        {
            final int textWidth = FONT.width(text);
            final int textHeight = FONT.lineHeight;

            textX = this.getX() + (this.getWidth() / 2) - (textWidth / 2);
            textY = this.getY() + (this.getHeight() / 2) - (textHeight / 2);
        }
        else
        {
            textX = getX() + padding;
            textY = getY() + padding;
        }

        // Scaled text
        // g.drawString(FONT, text, textX, textY, textColor.asInt(), false);
        g.drawString(FONT, text, textX, textY, this.pressed ? clickColor.asInt() : textColor.asInt(), false);
    }

    @Override
    public void onClick(double x, double y)
    {
        if (isToggleButton) this.pressed = !this.pressed;
        else this.pressed = true;
    }

    @Override
    public void onRelease(double x, double y)
    {
        if (!isToggleButton) this.pressed = false;
        MouseButtonClick clickPoint = new MouseButtonClick(x, y);
        if (isMouseInButton(clickPoint)) onClickCallback.accept(clickPoint, pressed);
    }

    private boolean isMouseInButton(MouseButtonClick clickPoint)
    {
        // mouseX = clickPoint.x();
        // mouseY = clickPoint.y();

        // startX = this.getX();
        // startY = this.getY();
        // endX   = this.getRight();
        // endY   = this.getBottom();

        // return (mouseX >= startX && mouseX <= endX) // x axis check
        //         && (mouseY >= startY && clickPoint.y() <= endY); // y axis check

        return (clickPoint.x() >= this.getX() && clickPoint.x() <= this.getRight()) // x axis check
                && (clickPoint.y() >= this.getY() && clickPoint.y() <= this.getBottom()); // y axis check
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }

    /* GETTERS & SETTERS */

    public void setOnClick(BiConsumer<MouseButtonClick, Boolean> onClickCallback)
    {
        this.onClickCallback = onClickCallback;
    }

    public boolean isTextCentered()
    {
        return isTextCentered;
    }

    public void setTextCentered(boolean textCentered)
    {
        this.isTextCentered = textCentered;
    }

    public void setToggleButton(boolean toggleButton)
    {
        isToggleButton = toggleButton;
    }

    public boolean isPressed()
    {
        return pressed;
    }

    public void setPressed(boolean pressed)
    {
        this.pressed = pressed;
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public Color getOutlineColor()
    {
        return outlineColor;
    }

    public void setOutlineColor(Color outlineColor)
    {
        this.outlineColor = outlineColor;
    }

    public Color getBackgroundColor()
    {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public Color getTextColor()
    {
        return this.textColor;
    }

    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
    }

    public Color getHoverColor()
    {
        return this.hoverColor;
    }

    public void setHoverColor(Color hoverColor)
    {
        this.hoverColor = hoverColor;
    }

    public Color getClickColor()
    {
        return this.clickColor;
    }

    public void setClickColor(Color clickColor)
    {
        this.clickColor = clickColor;
    }

    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    public int getOutlineThickness()
    {
        return outlineThickness;
    }

    public float getScale()
    {
        return scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    /* Builder */

    public static Builder builder(int x, int y, String text)
    {
        return new Builder(x, y, text);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private int width = 0;
        private int height = 0;
        private float scale = 1;
        private final String text;

        private int padding = 5;
        private boolean isRounded = false;
        private Color bgColor = Colors.BLACK.withHalfAlpha();
        private Color fgColor = Colors.WHITE;
        private Color hoverColor = Colors.WHITE;
        private Color clickColor = Colors.YELLOW;
        private boolean textCentered = false;
        private boolean toggleButton = false;
        private boolean pressed = false;

        private Color outlineColor = Colors.CLEAR;
        private int outlineThickness = 1;

        private BiConsumer<MouseButtonClick, Boolean> onClick = (e, pressed) -> {
        };

        private Builder(int x, int y, String text)
        {
            this.x = x;
            this.y = y;
            this.text = text;
        }

        public Builder width(int width)
        {
            this.width = width;
            return this;
        }

        public Builder height(int height)
        {
            this.height = height;
            return this;
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

        public Builder bgColor(Color bgColor)
        {
            this.bgColor = bgColor;
            return this;
        }

        public Builder fgColor(Color fgColor)
        {
            this.fgColor = fgColor;
            return this;
        }

        public Builder hoverColor(Color hoverColor)
        {
            this.hoverColor = hoverColor;
            return this;
        }

        public Builder clickColor(Color clickColor)
        {
            this.clickColor = clickColor;
            return this;
        }

        public Builder centeredText(boolean centered)
        {
            this.textCentered = centered;
            return this;
        }

        public Builder toggleButton(boolean toggle)
        {
            this.toggleButton = toggle;
            return this;
        }

        public Builder pressed(boolean pressed)
        {
            this.pressed = pressed;
            return this;
        }

        public Builder outlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return this;
        }

        public Builder outlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return this;
        }

        public Builder scale(float scale)
        {
            this.scale = scale;
            return this;
        }

        public Builder onClick(BiConsumer<MouseButtonClick, Boolean> onClick)
        {
            this.onClick = onClick;
            return this;
        }

        public SearchResultWidget build()
        {
            SearchResultWidget button = new SearchResultWidget(
                    x,
                    y,
                    padding,
                    isRounded,
                    text,
                    outlineThickness,
                    bgColor,
                    fgColor,
                    hoverColor,
                    clickColor,
                    scale
            );

            button.setTextCentered(textCentered);
            button.setToggleButton(toggleButton);
            button.setOutlineColor(this.outlineColor);
            button.setOnClick(onClick);

            button.pressed = this.pressed;

            if (this.width > 0) button.setWidth(this.width);
            if (this.height > 0) button.setHeight(this.height);

            return button;
        }
    }
}
