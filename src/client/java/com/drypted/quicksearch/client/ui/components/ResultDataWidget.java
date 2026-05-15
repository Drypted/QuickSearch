package com.drypted.quicksearch.client.ui.components;

import com.drypted.quicksearch.client.core.blueprints.ui.ScrollBoxWidgetEntry;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Color;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Colors;
import com.drypted.quicksearch.client.core.blueprints.ui.common.MouseButtonClick;
import com.drypted.quicksearch.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.quicksearch.client.ui.renderer.RenderCommon;
import com.drypted.quicksearch.client.ui.styling.Styles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/// Base widget for result data entries. Handles all shared rendering (background, outline, hover/click/selected state)
/// and input logic. Subclasses provide the content rendered inside the padded area.
public class ResultDataWidget extends AbstractWidget implements ScrollBoxWidgetEntry
{
    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 6;

    private static final int SUBTITLE_SPACING = 1;
    private static final float SUBTITLE_SCALE = 0.75f;

    private final @Nullable ItemStack icon;
    private final @Nullable String title;
    private final @Nullable String subtitle;

    private final int paddingX;
    private final int paddingY;
    private final boolean isRounded;
    private final float outlineThickness;
    private Color backgroundColor;
    private Color textColor;
    private Color hoverColor;
    private Color clickColor;
    private Color selectedColor;
    private Color outlineColor;
    private boolean pressed;
    private boolean selected;
    private boolean showOutline;
    private boolean disabled;

    // callback
    private BiConsumer<MouseButtonClick, Boolean> onClickCallback = (e, pressed) -> {
    };

    private ResultDataWidget(int x, int y, @Nullable ItemStack icon, @Nullable String title, @Nullable String subtitle, int width, int paddingX, int paddingY, boolean isRounded, float outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor, Color selectedColor, Color outlineColor)
    {
        super(x, y, width, 0, Component.empty());
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
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

        //        this.setHeight((2 * paddingY) + Math.max(
        //                ICON_PADDING,
        //                getFont().lineHeight + SUBTITLE_SPACING + (int) (getFont().lineHeight * SUBTITLE_SCALE)
        //        ));
        boolean shouldRenderIcon = this.icon != null && !this.icon.isEmpty() && this.icon.getItem() != Items.AIR;
        boolean shouldRenderTitle = this.title != null && !this.title.isEmpty();
        boolean shouldRenderSubtitle = this.subtitle != null && !this.subtitle.isEmpty();
        this.setHeight((2 * paddingY) + Math.max(
                shouldRenderIcon ? ICON_SIZE : 0, //
                (shouldRenderTitle ? getFont().lineHeight : 0) + //
                        (shouldRenderSubtitle ? SUBTITLE_SPACING + (int) (getFont().lineHeight * SUBTITLE_SCALE) : 0)
        ));
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

        RenderCommon.drawRectangle(
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
                (int) (startPosX + paddingX + outlineThickness),
                (int) (startPosY + outlineThickness),
                (int) (endPosX - paddingX - outlineThickness),
                (int) (endPosY - outlineThickness)
        );
        renderContent(g, startPosX, startPosY);
        g.disableScissor();
    }

    private void renderContent(@NonNull GuiGraphics g, int startPosX, int startPosY)
    {
        // icon
        int iconX = (int) (startPosX + outlineThickness + paddingX);
        int iconY = startPosY + paddingY;

        boolean shouldRenderIcon = this.icon != null && !this.icon.isEmpty() && this.icon.getItem() != Items.AIR;

        if (shouldRenderIcon) RenderCommon.drawScaledItemSize(g, this.icon, iconX, iconY, ICON_SIZE);

        // title
        int titleX = iconX + (shouldRenderIcon ? (ICON_SIZE + ICON_PADDING) : 0);
        int titleY = startPosY + paddingY;
        Color _textColor = this.isDisabled() ? textColor.withLightness(textColor.getLightness() / 2) : textColor;

        boolean shouldRenderTitle = this.title != null && !this.title.isEmpty();
        if (shouldRenderTitle) g.drawString(getFont(), this.title, titleX, titleY, _textColor.asInt(), false);

        // subtitle
        boolean shouldRenderSubtitle = this.subtitle != null && !this.subtitle.isEmpty();

        if (shouldRenderSubtitle)
        {
            int subtitleY = startPosY + paddingY;
            // add spacing if title is also rendered
            if (shouldRenderTitle) subtitleY += getFont().lineHeight + SUBTITLE_SPACING;

            RenderCommon.drawScaledText(g, this.subtitle, SUBTITLE_SCALE, titleX, subtitleY, _textColor, false);
        }

        // show bind, will be used to quick nav; disabled for now
        // if (this.shouldShowBind())
        // {
        //     final int size = 8;
        //     RenderCommon.drawText(
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
    public void onClick(@NonNull MouseButtonEvent mEv, boolean doubleClick)
    {
        if (this.isDisabled()) return;
        this.pressed = true;
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent mEv)
    {
        if (this.isDisabled()) return;

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
        if (this.isDisabled()) return;
        this.pressed = true;
        onClickCallback.accept(MouseButtonClick.from(getX(), getY()), true);
    }

    @Override
    public void unpress()
    {
        if (this.isDisabled()) return;
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

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    /* STATICS */

    private static Font getFont()
    {
        return Minecraft.getInstance().font;
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput)
    {
    }

    /* BUILDER */

    public static Builder builder(int x, int y, @Nullable ItemStack icon, @Nullable String title, @Nullable String subtitle)
    {
        return new Builder(x, y, icon, title, subtitle);
    }

    public static class Builder
    {
        private final int x;
        private final int y;
        private final @Nullable ItemStack icon;
        private final @Nullable String title;
        private final @Nullable String subtitle;

        private int width = 0;
        private int paddingX = 7;
        private int paddingY = 6;
        private boolean isRounded = true;
        private Color backgroundColor = Styles.ResultData.BACKGROUND_COLOR;
        private Color textColor = Styles.ResultData.TEXT_COLOR;
        private Color hoverColor = Styles.ResultData.HOVER_OUTLINE_COLOR;
        private Color clickColor = Styles.ResultData.CLICKED_OUTLINE_COLOR;
        private Color selectedColor = Styles.ResultData.SELECTED_OUTLINE_COLOR;
        private Color outlineColor = Colors.CLEAR;
        private boolean pressed = false;
        private boolean showOutline = false;
        private float outlineThickness = Styles.ResultData.OUTLINE_THICKNESS;

        private boolean disabled = false;

        private BiConsumer<MouseButtonClick, Boolean> onClick = (e, p) -> {
        };

        private Builder(int x, int y, @Nullable ItemStack icon, @Nullable String title, @Nullable String subtitle)
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

        public Builder disabled(boolean disabled)
        {
            this.disabled = disabled;
            return this;
        }

        public ResultDataWidget build()
        {
            ResultDataWidget widget = new ResultDataWidget(
                    x,
                    y,
                    icon,
                    title,
                    subtitle,
                    width,
                    paddingX,
                    paddingY,
                    isRounded,
                    outlineThickness,
                    backgroundColor,
                    textColor,
                    hoverColor,
                    clickColor,
                    selectedColor,
                    outlineColor
            );

            widget.setOnClickCallback(onClick);
            widget.setOutlineEnabled(showOutline);
            widget.pressed = this.pressed;
            widget.setDisabled(this.disabled);
            if (this.width > 0) widget.setWidth(this.width);

            return widget;
        }
    }
}