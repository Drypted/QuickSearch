package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.Minecraft;
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

    public SearchBarWidget(int x, int y, int width, int height, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, height, Component.empty());
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
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

        int textX = this.getX() + 6;
        int textY = this.getY() + (this.getHeight() - 8) / 2;

        // text
        guiGraphics.drawString(Minecraft.getInstance().font, this.text, textX, textY, Colors.iWHITE, false);

        // caret
        if ((System.currentTimeMillis() / 500) % 2 == 0)
        {
            int caretX = textX + Minecraft.getInstance().font.width(this.text);
            guiGraphics.fill(caretX, textY, caretX + 1, textY + 8, canType ? Colors.iWHITE : Colors.iYELLOW);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!this.canType)
            return false;

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
    }`

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
