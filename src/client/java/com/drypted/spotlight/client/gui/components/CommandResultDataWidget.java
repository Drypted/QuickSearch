package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.NonNull;

public class CommandResultDataWidget extends BaseResultDataWidget
{
    private final Command command;
    private final boolean _isPressable;

    public CommandResultDataWidget(int x, int y, int width, Command command, int paddingX, int paddingY, boolean isRounded, int outlineThickness, Color backgroundColor, Color textColor, Color hoverColor, Color clickColor, Color selectedColor, Color outlineColor)
    {
        super(
                x,
                y,
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
        this.command = command;

        // clickable only if command requires no args
        this._isPressable = !command.requiresArgs();

        this.setHeight((2 * paddingY) + getFont().lineHeight + SUBTITLE_SPACING + (int) (getFont().lineHeight * SUBTITLE_SCALE));
    }

    @Override
    protected boolean isPressable()
    {
        return _isPressable;
    }

    @Override
    protected void renderContent(@NonNull GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY)
    {
        // title
        int titleX = startPosX + paddingX;
        int titleY = startPosY + paddingY;
        Color _textColor = this._isPressable ? textColor : textColor.withLightness(textColor.getLightness() / 2);

        g.drawString(getFont(), this.command.getName(), titleX, titleY, _textColor.asInt(), false);

        // subtitle
        int subtitleY = titleY + getFont().lineHeight + SUBTITLE_SPACING;
        float subtitleScale = 0.75f;

        RenderUtils.drawScaledText(g, this.command.getDescription(), subtitleScale, titleX, subtitleY, _textColor, false);

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

    public static final class Builder extends BaseBuilder<CommandResultDataWidget, Builder>
    {
        private final Command command;

        private Builder(int x, int y, Command command)
        {
            super(x, y);
            this.command = command;
            // CommandResultDataWidget has asymmetric default padding
            this.paddingX = 10;
            this.paddingY = 5;
        }

        @Override
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

            applySharedState(button);

            return button;
        }
    }
}