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

import static com.drypted.spotlight.client.RendererUtils.drawScaledItem;

public class SearchResultWidget extends AbstractWidget
{
    private static final Font FONT = Minecraft.getInstance().font;

    private Block icon;
    private String title;
    private String subtitle;

    private final int padding;
    private final boolean isRounded;
    private final int outlineThickness;
    private Color backgroundColor;
    private Color textColor;
    private Color hoverColor;
    private Color clickColor;
    private boolean pressed;
    private boolean highlighted;

    private Color outlineColor = Colors.CLEAR;

    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 6;
    private static final int SUBTITLE_SPACING = 1;
    private static final float SUBTITLE_SCALE = 0.75f;

    // callback
    private BiConsumer<MouseButtonClick, Boolean> onClickCallback = (e, pressed) -> {
    };

    public SearchResultWidget(int x, int y, int width, Block icon, String title, String subtitle, int padding, boolean isRounded, int outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor)
    {
        super(x, y, width, 0, Component.empty());
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.padding = padding;
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;

        this.setHeight((2 * padding) + Math.max(
                ICON_PADDING,
                FONT.lineHeight + SUBTITLE_SPACING + (int) (FONT.lineHeight * SUBTITLE_SCALE)
        ));
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

        // icon
        int iconX = startPosX + padding;
        int iconY = startPosY + padding;

        drawScaledItem(g, this.icon.asItem().getDefaultInstance(), iconX, iconY, ICON_SIZE);

        // title
        int titleX = iconX + ICON_SIZE + ICON_PADDING;
        int titleY = startPosY + padding;

        g.drawString(FONT, this.title, titleX, titleY, textColor.asInt(), false);

        // subtitle
        int subtitleY = titleY + FONT.lineHeight + SUBTITLE_SPACING;
        float subtitleScale = 0.75f;

        g.pose().pushPose();
        g.pose().scale(subtitleScale, subtitleScale, subtitleScale);
        g.drawString(
                FONT,
                this.subtitle,
                (int) (titleX / subtitleScale),
                (int) (subtitleY / subtitleScale),
                textColor.asInt(),
                false
        );
        g.pose().popPose();
    }

    /* Input */

    @Override
    public void onClick(double x, double y)
    {
        this.pressed = true;
    }

    @Override
    public void onRelease(double x, double y)
    {
        this.pressed = false;
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

    /* State Methods */

    public boolean isPressed()
    {
        return pressed;
    }

    public boolean isHighlighted()
    {
        return highlighted;
    }

    /* GETTERS & SETTERS */

    public void setOnClickCallback(BiConsumer<MouseButtonClick, Boolean> onClickCallback)
    {
        this.onClickCallback = onClickCallback;
    }

    public Block getIcon()
    {
        return icon;
    }

    public void setIcon(Block icon)
    {
        this.icon = icon;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public Color getTextColor()
    {
        return textColor;
    }

    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
    }

    public Color getHoverColor()
    {
        return hoverColor;
    }

    public void setHoverColor(Color hoverColor)
    {
        this.hoverColor = hoverColor;
    }

    public Color getClickColor()
    {
        return clickColor;
    }

    public void setClickColor(Color clickColor)
    {
        this.clickColor = clickColor;
    }

    public Color getOutlineColor()
    {
        return outlineColor;
    }

    public void setOutlineColor(Color outlineColor)
    {
        this.outlineColor = outlineColor;
    }

    /* Builder */

    public static Builder builder(int x, int y, Block icon, String title, String subtitle)
    {
        return new Builder(x, y, icon, title, subtitle);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private int width = 0;
        private final Block icon;
        private final String title;
        private final String subtitle;

        private int padding = 5;
        private boolean isRounded = false;
        private Color backgroundColor = Colors.GRAY.withHalfAlpha();
        private Color textColor = Colors.WHITE;
        private Color hoverColor = Colors.WHITE;
        private Color clickColor = Colors.YELLOW;
        private boolean pressed = false;

        private Color outlineColor = Colors.CLEAR;
        private int outlineThickness = 1;

        private BiConsumer<MouseButtonClick, Boolean> onClick = (e, pressed) -> {
        };

        private Builder(int x, int y, Block icon, String title, String subtitle)
        {
            this.x = x;
            this.y = y;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
        }

        public Builder width(int width)
        {
            this.width = width;
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
            this.backgroundColor = bgColor;
            return this;
        }

        public Builder fgColor(Color fgColor)
        {
            this.textColor = fgColor;
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


        public Builder onClick(BiConsumer<MouseButtonClick, Boolean> onClick)
        {
            this.onClick = onClick;
            return this;
        }

        public SearchResultWidget build()
        {
            SearchResultWidget button = new SearchResultWidget(
                    this.x,
                    this.y,
                    this.width,
                    this.icon,
                    this.title,
                    this.subtitle,
                    this.padding,
                    this.isRounded,
                    this.outlineThickness,
                    this.backgroundColor,
                    this.textColor,
                    this.hoverColor,
                    this.clickColor
            );

            button.setOutlineColor(this.outlineColor);
            button.setOnClickCallback(onClick);

            button.pressed = this.pressed;

            if (this.width > 0) button.setWidth(this.width);

            return button;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
