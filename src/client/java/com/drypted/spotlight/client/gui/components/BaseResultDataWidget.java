package com.drypted.spotlight.client.gui.components;

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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;

/// Base widget for result data entries. Handles all shared rendering (background, outline, hover/click/selected state)
/// and input logic. Subclasses provide the content rendered inside the padded area.
public abstract class BaseResultDataWidget extends AbstractWidget implements ScrollBoxWidgetEntry
{
    protected static final int SUBTITLE_SPACING = 1;
    protected static final float SUBTITLE_SCALE = 0.75f;

    protected final int paddingX;
    protected final int paddingY;
    protected final boolean isRounded;
    protected final int outlineThickness;
    protected Color backgroundColor;
    protected Color textColor;
    protected Color hoverColor;
    protected Color clickColor;
    protected Color selectedColor;
    protected Color outlineColor;
    protected boolean pressed;
    protected boolean selected;
    protected boolean showOutline;

    // callback
    private BiConsumer<MouseButtonClick, Boolean> onClickCallback = (e, pressed) -> {
    };

    protected BaseResultDataWidget(int x, int y, int width, int paddingX, int paddingY, boolean isRounded, int outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor, Color selectedColor, Color outlineColor)
    {
        super(x, y, width, 0, Component.empty());
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
    }

    @Override
    protected final void renderWidget(@NonNull GuiGraphics g, int mouseX, int mouseY, float delta)
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

        g.enableScissor(
                startPosX + paddingX + outlineThickness,
                startPosY + outlineThickness,
                endPosX - paddingX - outlineThickness,
                endPosY - outlineThickness
        );
        renderContent(g, startPosX, startPosY, endPosX, endPosY);
        g.disableScissor();
    }

    /// Render the widget-specific content (icon, title, subtitle, etc.) inside the scissored region. Coordinates passed
    /// are the raw start/end positions of the full widget bounds.
    protected abstract void renderContent(@NonNull GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY);

    /// Whether this widget should respond to press/release input. Override to return false to disable interaction.
    protected boolean isPressable()
    {
        return true;
    }

    /* Input */

    @Override
    public void onClick(@NonNull MouseButtonEvent mEv, boolean doubleClick)
    {
        if (!this.isPressable()) return;
        this.pressed = true;
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent mEv)
    {
        if (!this.isPressable()) return;

        this.pressed = false;
        MouseButtonClick clickPoint = MouseButtonClick.from(mEv);
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
        if (!this.isPressable()) return;
        this.pressed = true;
        onClickCallback.accept(MouseButtonClick.from(getX(), getY()), true);
    }

    @Override
    public void unpress()
    {
        if (!this.isPressable()) return;
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

    /* STATICS */

    protected static Font getFont()
    {
        return Minecraft.getInstance().font;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }

    /* Base Builder */

    /// Shared builder state. Subclass builders should extend this with a covariant return type on each setter.
    @SuppressWarnings("unchecked")
    protected static abstract class BaseBuilder<W extends BaseResultDataWidget, B extends BaseBuilder<W, B>>
    {
        protected final int x;
        protected final int y;
        protected int width = 0;

        protected int paddingX = 5;
        protected int paddingY = 5;
        protected boolean isRounded = false;
        protected Color backgroundColor = Styles.ResultData.BACKGROUND_COLOR;
        protected Color textColor = Styles.ResultData.TEXT_COLOR;
        protected Color hoverColor = Styles.ResultData.HOVER_OUTLINE_COLOR;
        protected Color clickColor = Styles.ResultData.CLICKED_OUTLINE_COLOR;
        protected Color selectedColor = Styles.ResultData.SELECTED_OUTLINE_COLOR;
        protected Color outlineColor = Colors.CLEAR;
        protected boolean pressed = false;
        protected boolean showOutline = false;
        protected int outlineThickness = Styles.ResultData.OUTLINE_THICKNESS;

        protected BiConsumer<MouseButtonClick, Boolean> onClick = (e, p) -> {
        };

        protected BaseBuilder(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public B width(int width)
        {
            this.width = width;
            return (B) this;
        }

        public B paddingX(int paddingX)
        {
            this.paddingX = paddingX;
            return (B) this;
        }

        public B paddingY(int paddingY)
        {
            this.paddingY = paddingY;
            return (B) this;
        }

        public B isRounded(boolean isRounded)
        {
            this.isRounded = isRounded;
            return (B) this;
        }

        public B bgColor(Color bgColor)
        {
            this.backgroundColor = bgColor;
            return (B) this;
        }

        public B fgColor(Color fgColor)
        {
            this.textColor = fgColor;
            return (B) this;
        }

        public B hoverColor(Color hoverColor)
        {
            this.hoverColor = hoverColor;
            return (B) this;
        }

        public B clickColor(Color clickColor)
        {
            this.clickColor = clickColor;
            return (B) this;
        }

        public B selectedColor(Color selectedColor)
        {
            this.selectedColor = selectedColor;
            return (B) this;
        }

        public B outlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return (B) this;
        }

        public B pressed(boolean pressed)
        {
            this.pressed = pressed;
            return (B) this;
        }

        public B showOutline(boolean showOutline)
        {
            this.showOutline = showOutline;
            return (B) this;
        }

        public B outlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return (B) this;
        }

        public B onClick(BiConsumer<MouseButtonClick, Boolean> onClick)
        {
            this.onClick = onClick;
            return (B) this;
        }

        protected void applySharedState(W widget)
        {
            widget.setOnClickCallback(onClick);
            widget.setOutlineEnabled(showOutline);
            widget.pressed = this.pressed;
            if (this.width > 0) widget.setWidth(this.width);
        }

        public abstract W build();
    }
}