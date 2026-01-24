package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.records.RoundedCorners;
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

import java.util.ArrayList;
import java.util.function.Consumer;

public class SearchInputWidget extends AbstractWidget
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

    private final ArrayList<Consumer<String>> onTypeCallbacks = new ArrayList<>();

    public SearchInputWidget(int x, int y, int width, int height, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, height, Component.empty());
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;

        // calculate text position
        TextX = this.getX() + TEXT_PADDING_X;
        TextY = this.getY() + (height - FONT.lineHeight) / 2;
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
                RoundedCorners.fromSingle(this.isRounded),
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


    /* Draw */

    private void drawCaret(GuiGraphics guiGraphics)
    {
        if ((System.currentTimeMillis() / 500) % 2 == 0)
        {
            int caretX = TextX + Minecraft.getInstance().font.width(this.text);
            guiGraphics.fill(caretX, TextY, caretX + 1, TextY + 8, canType ? Colors.iWHITE : Colors.iYELLOW);
        }
    }

    /* Meta (Stuff to make it functional) */

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!this.canType)
            return false;

        if (StringUtil.isAllowedChatCharacter(codePoint))
        {
            this.text += codePoint;
            for (Consumer<String> callback : this.onTypeCallbacks)
            {
                callback.accept(this.text);
            }
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
            for (Consumer<String> callback : this.onTypeCallbacks)
            {
                callback.accept(this.text);
            }
            return true;
        }

        if (this.canType)
        {
            // erase on backspace
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.text.isEmpty())
            {
                this.text = this.text.substring(0, this.text.length() - 1);
                for (Consumer<String> callback : this.onTypeCallbacks)
                {
                    callback.accept(this.text);
                }
                return true;
            }
        }

        return false;
    }

    /* Getters and Setters */

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public Color getOutlineColor()
    {
        return outlineColor;
    }

    public int getOutlineThickness()
    {
        return outlineThickness;
    }

    public String getText()
    {
        return text;
    }

    /* Methods */

    public void clearText()
    {
        this.text = "";
    }

    public boolean hasText()
    {
        return !this.text.isEmpty();
    }

    public void subscribeToTypeCallback(Consumer<String> onTypeCallback)
    {
        this.onTypeCallbacks.add(onTypeCallback);
    }

    /* Builder */

    public static Builder builder(int x, int y, int width, int height)
    {
        return new Builder(x, y, width, height);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private final int width;
        private int height;
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

        public Builder height(int height)
        {
            this.height = height;
            return this;
        }

        public Builder isRounded(boolean isRounded)
        {
            this.isRounded = isRounded;
            return this;
        }

        public Builder outlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return this;
        }

        public Builder backgroundColor(Color backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder outlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return this;
        }

        public SearchInputWidget build()
        {
            return new SearchInputWidget(x, y, width, height, isRounded, outlineThickness, backgroundColor, outlineColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
