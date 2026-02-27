package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import com.drypted.spotlight.client.models.ItemsResultData;
import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.NonNull;

/// Renders a `ItemsResultData` entry as a widget, implements `ScrollBoxWidgetEntry`
public class ItemsResultDataWidget extends BaseResultDataWidget
{
    private final ItemsResultData data;

    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 6;

    public ItemsResultDataWidget(int x, int y, int width, ItemsResultData data, int padding, boolean isRounded, int outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor, Color selectedColor, Color outlineColor)
    {
        super(
                x,
                y,
                width,
                padding,
                padding,
                isRounded,
                outlineThickness,
                backgroundColor,
                textColor,
                hoverColor,
                clickColor,
                selectedColor,
                outlineColor
        );
        this.data = data;

        this.setHeight((2 * padding) + Math.max(
                ICON_PADDING,
                getFont().lineHeight + SUBTITLE_SPACING + (int) (getFont().lineHeight * SUBTITLE_SCALE)
        ));
    }

    @Override
    protected void renderContent(@NonNull GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY)
    {
        // icon
        int iconX = startPosX + paddingX;
        int iconY = startPosY + paddingY;

        RenderUtils.drawScaledItemSize(g, this.data.getIcon(), iconX, iconY, ICON_SIZE);

        // title
        int titleX = iconX + ICON_SIZE + ICON_PADDING;
        int titleY = startPosY + paddingY;

        g.drawString(getFont(), this.data.getName(), titleX, titleY, textColor.asInt(), false);

        // subtitle
        int subtitleY = titleY + getFont().lineHeight + SUBTITLE_SPACING;
        float subtitleScale = 0.75f;

        RenderUtils.drawScaledText(
                g,
                this.data.getIdentifier().toString(),
                subtitleScale,
                titleX,
                subtitleY,
                textColor,
                false
        );

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

    /* GETTERS */

    public ItemsResultData getData()
    {
        return data;
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

    public static Builder builder(int x, int y, ItemsResultData data)
    {
        return new Builder(x, y, data);
    }

    public static final class Builder extends BaseBuilder<ItemsResultDataWidget, Builder>
    {
        private final ItemsResultData data;

        // ItemsResultDataWidget uses a single uniform padding; paddingX and paddingY are always equal.
        // The base builder exposes both, but the convenience padding() method keeps them in sync.
        private Builder(int x, int y, ItemsResultData data)
        {
            super(x, y);
            this.data = data;
        }

        /// Sets both paddingX and paddingY to the same value (uniform padding).
        public Builder padding(int padding)
        {
            this.paddingX = padding;
            this.paddingY = padding;
            return this;
        }

        @Override
        public ItemsResultDataWidget build()
        {
            ItemsResultDataWidget button = new ItemsResultDataWidget(
                    this.x,
                    this.y,
                    this.width,
                    this.data,
                    this.paddingX,
                    this.isRounded,
                    this.outlineThickness,
                    this.backgroundColor,
                    this.textColor,
                    this.hoverColor,
                    this.clickColor,
                    this.selectedColor,
                    this.outlineColor
            );

            applySharedState(button);

            return button;
        }
    }
}