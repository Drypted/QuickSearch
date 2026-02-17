package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.gui.models.MouseButtonClick;
import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.models.ScrollBoxWidgetEntry;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import com.drypted.spotlight.client.styling.Styles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public class CommandResultDataWidget extends AbstractWidget implements ScrollBoxWidgetEntry
{
    private static final Font FONT = Minecraft.getInstance().font;

    private final Command command;
    private final boolean isPressable;

    private final int paddingX;
    private final int paddingY;
    private final boolean isRounded;
    private final int outlineThickness;
    private Color backgroundColor;
    private Color textColor;
    private Color hoverColor;
    private Color clickColor;
    private Color selectedColor;
    private Color outlineColor;
    private boolean pressed;
    private boolean selected;
    private boolean showOutline;

    private static final int SUBTITLE_SPACING = 1;
    private static final float SUBTITLE_SCALE = 0.75f;

    // callback
    private BiConsumer<MouseButtonClick, Boolean> onClickCallback = (e, pressed) -> {
    };

    public CommandResultDataWidget(int x, int y, int width, Command command, int paddingX, int paddingY, boolean isRounded, int outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor, Color selectedColor, Color outlineColor)
    {
        super(x, y, width, 0, Component.empty());
        this.command = command;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;
        this.selectedColor = selectedColor;
        this.outlineColor = outlineColor;

        // clickable only if commands is no args
        this.isPressable = !command.requiresArgs();

        this.setHeight((2 * paddingY) + FONT.lineHeight + SUBTITLE_SPACING + (int) (FONT.lineHeight * SUBTITLE_SCALE));
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
        if (showOutline) outlineColor = this.getOutlineColor();
        else if (this.pressed) outlineColor = clickColor;
        else if (this.isHovered) outlineColor = hoverColor;
        else outlineColor = backgroundColor;

        boolean renderOutline = this.isHovered() || this.isPressed() || this.isFocused();

        RenderUtils.drawRectangle(
                g,
                startPosX,
                startPosY,
                endPosX,
                endPosY,
                RoundedCorners.fromSingle(this.isRounded),
                this.outlineThickness,
                this.selected || renderOutline,
                this.backgroundColor,
                (this.selected && !this.isPressed()) ? selectedColor : outlineColor
        );

        // title
        int titleX = startPosX + paddingX;
        int titleY = startPosY + paddingY;
        int _textColor = this.isPressable ? textColor.asInt() : textColor.withLightness(textColor.getLightness() / 2).asInt();

        g.drawString(FONT, this.command.getName(), titleX, titleY, _textColor, false);

        // subtitle
        int subtitleY = titleY + FONT.lineHeight + SUBTITLE_SPACING;
        float subtitleScale = 0.75f;

        g.pose().pushPose();
        g.pose().scale(subtitleScale, subtitleScale, subtitleScale);
        g.drawString(
                FONT,
                this.command.getDescription(),
                (int) (titleX / subtitleScale),
                (int) (subtitleY / subtitleScale),
                _textColor,
                false
        );
        g.pose().popPose();

        // show bind, will be used to quick nav; disabled for now
        // if (this.shouldShowBind())
        // {
        //     final int size = 8;
        //     RenderUtils.drawText(
        //             g,
        //             "O",
        //             0.75f,
        //             endPosX - size,
        //             endPosY - size,
        //             endPosX,
        //             endPosY,
        //             RoundedCorners.from(true, false, false, false),
        //             Colors.HIGHLIGHT_YELLOW,
        //             textColor
        //     );
        // }
    }

    /* Input */

    @Override
    public void onClick(double x, double y)
    {
        if (!this.isPressable) return;
        this.pressed = true;
    }

    @Override
    public void onRelease(double x, double y)
    {
        if (!this.isPressable) return;

        this.pressed = false;
        MouseButtonClick clickPoint = new MouseButtonClick(x, y);
        if (isMouseInButton(clickPoint))
        {
            onClickCallback.accept(clickPoint, pressed);
        }
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

    @Override
    public void select(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public void press()
    {
        if (!this.isPressable) return;
        this.pressed = true;
        onClickCallback.accept(MouseButtonClick.from(getX(), getY()), true);
    }

    @Override
    public void unpress()
    {
        if (!this.isPressable) return;
        this.pressed = false;
    }

    /* GETTERS & SETTERS */

    public void setOnClickCallback(BiConsumer<MouseButtonClick, Boolean> onClickCallback)
    {
        this.onClickCallback = onClickCallback;
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

    public Color getSelectedColor()
    {
        return selectedColor;
    }

    public void setSelectedColor(Color selectedColor)
    {
        this.selectedColor = selectedColor;
    }

    public void setOutlineEnabled(boolean enabled)
    {
        this.showOutline = enabled;
    }

    // public boolean shouldShowBind()
    // {
    //     return showBind;
    // }
    //
    // public void setShowBind(boolean showBind)
    // {
    //     this.showBind = showBind;
    // }

    /* Builder */

    public static Builder builder(int x, int y, Command data)
    {
        return new Builder(x, y, data);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private int width = 0;
        private final Command command;

        private int paddingX = 10;
        private int paddingY = 5;
        private boolean isRounded = false;
        private Color backgroundColor = Styles.ResultData.BACKGROUND_COLOR;
        private Color textColor = Styles.ResultData.TEXT_COLOR;
        private Color hoverColor = Styles.ResultData.HOVER_OUTLINE_COLOR;
        private Color clickColor = Styles.ResultData.CLICKED_OUTLINE_COLOR;
        private Color selectedColor = Styles.ResultData.SELECTED_OUTLINE_COLOR;
        private Color outlineColor = Colors.CLEAR;
        private boolean pressed = false;
        private boolean showOutline = false;

        private int outlineThickness = 1;

        private BiConsumer<MouseButtonClick, Boolean> onClick = (e, pressed) -> {
        };

        private Builder(int x, int y, Command command)
        {
            this.x = x;
            this.y = y;
            this.command = command;
        }

        public Builder width(int width)
        {
            this.width = width;
            return this;
        }

        public Builder paddingX(int paddingX)
        {
            this.paddingX = paddingX;
            return this;
        }

        public Builder paddingY(int paddingY)
        {
            this.paddingY = paddingY;
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

        public Builder selectedColor(Color selectedColor)
        {
            this.selectedColor = selectedColor;
            return this;
        }

        public Builder outlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return this;
        }

        public Builder pressed(boolean pressed)
        {
            this.pressed = pressed;
            return this;
        }

        public Builder showOutline(boolean showOutline)
        {
            this.showOutline = showOutline;
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

        public CommandResultDataWidget build()
        {
            CommandResultDataWidget button = new CommandResultDataWidget(
                    this.x,
                    this.y,
                    this.width,
                    this.command,
                    this.paddingX,
                    this.paddingY,
                    this.isRounded,
                    this.outlineThickness,
                    this.backgroundColor,
                    this.textColor,
                    this.hoverColor,
                    this.clickColor,
                    this.selectedColor,
                    this.outlineColor
            );

            button.setOnClickCallback(onClick);
            button.setOutlineEnabled(showOutline);

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
