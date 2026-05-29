package com.drypted.quicksearch.client.ui.components;

import com.drypted.quicksearch.client.core.blueprints.ui.ScrollBoxWidgetEntry;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Color;
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

    private int paddingX;
    private int paddingY;
    private boolean isRounded;
    private final float outlineThickness;
    private Color backgroundColor;
    private Color outlineColor;
    private Color selectedBackgroundColor;
    private Color selectedOutlineColor;
    private Color clickedBackgroundColor;
    private Color clickedOutlineColor;
    private Color hoverBackgroundColor;
    private Color hoverOutlineColor;
    private Color textColor;
    private boolean pressed;
    private boolean selected;
    private boolean showOutline;
    private boolean disabled;

    // callback
    private BiConsumer<MouseButtonClick, Boolean> onClickCallback = (e, pressed) -> { };

    public ResultDataWidget(int x, int y, @Nullable ItemStack icon, @Nullable String title, @Nullable String subtitle)
    {
        super(x, y, 0, 0, Component.empty());
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.paddingX = Styles.ResultData.PADDING_X;
        this.paddingY = Styles.ResultData.PADDING_Y;
        this.isRounded = Styles.ResultData.ROUNDED;
        this.outlineThickness = Styles.ResultData.OUTLINE_THICKNESS;
        this.backgroundColor = Styles.ResultData.BACKGROUND_COLOR;
        this.outlineColor = Styles.ResultData.OUTLINE_COLOR;
        this.selectedBackgroundColor = Styles.ResultData.SELECTED_BACKGROUND_COLOR;
        this.selectedOutlineColor = Styles.ResultData.SELECTED_OUTLINE_COLOR;
        this.clickedBackgroundColor = Styles.ResultData.CLICKED_BACKGROUND_COLOR;
        this.clickedOutlineColor = Styles.ResultData.CLICKED_OUTLINE_COLOR;
        this.hoverBackgroundColor = Styles.ResultData.HOVER_BACKGROUND_COLOR;
        this.hoverOutlineColor = Styles.ResultData.HOVER_OUTLINE_COLOR;
        this.textColor = Styles.ResultData.TEXT_COLOR;

        recalculateHeight();
    }

    private void recalculateHeight()
    {
        boolean renderIcon = this.icon != null && !this.icon.isEmpty() && this.icon.getItem() != Items.AIR;
        boolean renderTitle = this.title != null && !this.title.isEmpty();
        boolean renderSubtitle = this.subtitle != null && !this.subtitle.isEmpty();
        this.setHeight((2 * paddingY) + Math.max(
                renderIcon ? ICON_SIZE : 0, //
                (renderTitle ? getFont().lineHeight : 0) + //
                        (renderSubtitle ? SUBTITLE_SPACING + (int) (getFont().lineHeight * SUBTITLE_SCALE) : 0)
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

        Color effectiveBackgroundColor = this.backgroundColor;
        Color effectiveOutlineColor = this.outlineColor;

        if (this.pressed)
        {
            effectiveBackgroundColor = this.clickedBackgroundColor;
            effectiveOutlineColor = this.clickedOutlineColor;
        }
        else if (this.selected)
        {
            effectiveBackgroundColor = this.selectedBackgroundColor;
            effectiveOutlineColor = this.selectedOutlineColor;
        }
        else if (this.isHovered)
        {
            effectiveBackgroundColor = this.hoverBackgroundColor;
            effectiveOutlineColor = this.hoverOutlineColor;
        }

        // toned down bg color if disabled
        effectiveBackgroundColor = disabled ? effectiveBackgroundColor.withHalfOpacity() : effectiveBackgroundColor;

        RenderCommon.drawRectangle(
                g,
                startPosX,
                startPosY,
                endPosX,
                endPosY,
                RoundedCorners.fromSingle(this.isRounded),
                this.outlineThickness,
                true,
                effectiveBackgroundColor,
                effectiveOutlineColor
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
        Color effectivetextColor = this.isDisabled()
                                   ? textColor.withLightness(textColor.getLightness() * (3.25f / 4))
                                   : textColor;

        boolean shouldRenderTitle = this.title != null && !this.title.isEmpty();
        if (shouldRenderTitle) g.drawString(getFont(), this.title, titleX, titleY, effectivetextColor.asInt(), false);

        // subtitle
        boolean shouldRenderSubtitle = this.subtitle != null && !this.subtitle.isEmpty();

        if (shouldRenderSubtitle)
        {
            int subtitleY = startPosY + paddingY;
            // add spacing if title is also rendered
            if (shouldRenderTitle) subtitleY += getFont().lineHeight + SUBTITLE_SPACING;

            RenderCommon.drawScaledText(g, this.subtitle, SUBTITLE_SCALE, titleX, subtitleY, effectivetextColor, false);
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
        this.pressed = true;
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent mEv)
    {
        this.pressed = false;

        if (!this.isDisabled())
        {
            MouseButtonClick clickPoint = MouseButtonClick.from(mEv);
            if (isMouseInButton(clickPoint))
            {
                onClickCallback.accept(clickPoint, pressed);
            }
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

    public boolean isPressed() { return pressed; }

    @Override
    public void select(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public void press()
    {
        this.pressed = true;
        if (!this.isDisabled()) onClickCallback.accept(MouseButtonClick.from(getX(), getY()), true);
    }

    @Override
    public void unpress()
    {
        this.pressed = false;
    }

    /* GETTERS & SETTERS */

    public void onClick(BiConsumer<MouseButtonClick, Boolean> onClick)
    {
        this.onClickCallback = onClick;
    }

    public void setOnClickCallback(BiConsumer<MouseButtonClick, Boolean> onClickCallback)
    {
        this.onClickCallback = onClickCallback;
    }

    public void setPaddingX(int paddingX)
    {
        this.paddingX = paddingX;
    }

    public void setPaddingY(int paddingY)
    {
        this.paddingY = paddingY;
        recalculateHeight();
    }

    public Color getBackgroundColor() { return backgroundColor; }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public Color getTextColor() { return textColor; }

    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
    }

    public Color getHoverColor() { return hoverOutlineColor; }

    public void setHoverColor(Color hoverColor)
    {
        this.hoverOutlineColor = hoverColor;
    }

    public Color getHoverBackgroundColor() { return hoverBackgroundColor; }

    public void setHoverBackgroundColor(Color hoverBackgroundColor)
    {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    public Color getClickColor() { return clickedOutlineColor; }

    public void setClickColor(Color clickColor)
    {
        this.clickedOutlineColor = clickColor;
    }

    public Color getClickedBackgroundColor() { return clickedBackgroundColor; }

    public void setClickedBackgroundColor(Color clickedBackgroundColor)
    {
        this.clickedBackgroundColor = clickedBackgroundColor;
    }

    public void setOutlineColor(Color outlineColor)
    {
        this.outlineColor = outlineColor;
    }

    public Color getSelectedColor() { return selectedOutlineColor; }

    public void setSelectedColor(Color selectedColor)
    {
        this.selectedOutlineColor = selectedColor;
    }

    public Color getSelectedBackgroundColor() { return selectedBackgroundColor; }

    public void setSelectedBackgroundColor(Color selectedBackgroundColor)
    {
        this.selectedBackgroundColor = selectedBackgroundColor;
    }

    public void setOutlineEnabled(boolean enabled)
    {
        this.showOutline = enabled;
    }

    public boolean isDisabled() { return disabled; }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    /* STATICS */

    private static Font getFont() { return Minecraft.getInstance().font; }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput)
    {
    }
}