package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.lwjgl.glfw.GLFW;

public class SearchBarWidget extends AbstractWidget
{
    private final boolean isRounded;
    private final int outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;

    private String text = "";
    private boolean canType = true;

    private static final Font FONT = Minecraft.getInstance().font;
    private static final int TEXT_PADDING_X = 6;
    private final int TextX;
    private final int TextY;

    public SearchBarWidget(int x, int y, int width, int height, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, height, Component.empty());
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;

        // calculate text position
        TextX = this.getX() + TEXT_PADDING_X;
        TextY = this.getY() + (this.getHeight() - FONT.lineHeight) / 2;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // background
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

        // text
        guiGraphics.drawString(Minecraft.getInstance().font, this.text, TextX, TextY, Colors.iWHITE, false);

        // caret
        drawCaret(guiGraphics);
    }

    private void drawCaret(GuiGraphics guiGraphics)
    {
        if ((System.currentTimeMillis() / 500) % 2 == 0)
        {
            int caretX = TextX + Minecraft.getInstance().font.width(this.text);
            guiGraphics.fill(caretX, TextY, caretX + 1, TextY + 8, canType ? Colors.iWHITE : Colors.iYELLOW);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!this.canType) return false;

        if (StringUtil.isAllowedChatCharacter(codePoint))
        {
            this.text += codePoint;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // use tab to toggle selecting a entry
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            this.canType = !this.canType;
            return true;
        }

        if (this.canType)
        {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.text.isEmpty())
            {
                this.text = this.text.substring(0, this.text.length() - 1);
                return true;
            }
        }

        return false;
    }

    public String getValue()
    {
        return text;
    }

    public int getOutlineThickness()
    {
        return outlineThickness;
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
