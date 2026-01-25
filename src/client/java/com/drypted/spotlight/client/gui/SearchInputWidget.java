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
    private static final Font FONT = Minecraft.getInstance().font;
    private static final int TEXT_PADDING_X = 6;

    private static final int INDICATOR_PADDING_RIGHT = 4;
    private static final float STATUS_TRANSITION_TIME_MS = 200f;

    private final boolean isRounded;
    private final int outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;
    private final Color focusedCaretColor;
    private final Color unfocusedCaretColor;

    private String text = "";

    private final int TextX;
    private final int TextY;

    private SearchStatus searchStatus = SearchStatus.IDLE;
    /// /// For transition animations; Disabled for now
    /// private long StatusChangeTime = 0L;

    /// Callbacks for when text is typed
    private final ArrayList<Consumer<String>> onTypeCallbacks = new ArrayList<>();

    public SearchInputWidget(int x, int y, int width, int height, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor, Color focusedCaretColor, Color unfocusedCaretColor)
    {
        super(x, y, width, height, Component.empty());
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
        this.focusedCaretColor = focusedCaretColor;
        this.unfocusedCaretColor = unfocusedCaretColor;

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
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                this.text,
                TextX,
                TextY,
                Colors.iWHITE,
                false
        );

        // caret
        drawCaret(guiGraphics);

        switch (this.searchStatus)
        {
            case IDLE:
                // do nothing
                break;
            case SEARCHING:
                drawLoadingAtEnd(guiGraphics);
                break;
        }
    }

    /* Draw */

    private void drawCaret(GuiGraphics guiGraphics)
    {
        final int blinkTimeMs = 500;

        if (System.currentTimeMillis() % (blinkTimeMs * 2) < blinkTimeMs)
        {
            int caretX = TextX + Minecraft.getInstance().font.width(this.text);
            guiGraphics.fill(
                    caretX,
                    TextY,
                    caretX + 1,
                    TextY + 8,
                    this.isFocused()
                    ? this.focusedCaretColor.asInt()
                    : this.unfocusedCaretColor.asInt()
            );
        }
    }

    private void drawLoadingAtEnd(GuiGraphics guiGraphics)
    {
        /// long elapsed = System.currentTimeMillis() - StatusChangeTime;
        /// float fadeIn = Math.min(1.0f, elapsed / STATUS_TRANSITION_TIME_MS); // 200ms fade-in

        int size = this.height - (2 * INDICATOR_PADDING_RIGHT);
        int loadingX = this.getX() + this.getWidth() - INDICATOR_PADDING_RIGHT - size;
        int loadingY = this.getY() + INDICATOR_PADDING_RIGHT;

        /// Color loadingColor = Colors.INFO_BLUE.withAlpha((int) (255 * fadeIn));
        RenderUtils.drawThreeDotPulseSpinner(
                guiGraphics,
                loadingX,
                loadingY,
                size,
                Colors.INFO_BLUE,
                System.currentTimeMillis()
        );
    }

    /* Meta (Stuff to make it functional) */

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        // no typing if not focused
        if (!this.isFocused())
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
            this.setFocused(true);
            for (Consumer<String> callback : this.onTypeCallbacks)
            {
                callback.accept(this.text);
            }
            return true;
        }

        if (this.isFocused())
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

    public SearchStatus getSearchStatus()
    {
        return searchStatus;
    }

    public void setSearchStatus(SearchStatus searchStatus)
    {
        /// if (this.searchStatus != searchStatus)
        /// {
        ///     this.StatusChangeTime = System.currentTimeMillis();
        /// }
        this.searchStatus = searchStatus;
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
        private Color focusedCaretColor = Colors.WHITE;
        private Color unfocusedCaretColor = Colors.YELLOW;

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

        public Builder focusedCaretColor(Color focusedCaretColor)
        {
            this.focusedCaretColor = focusedCaretColor;
            return this;
        }

        public Builder unfocusedCaretColor(Color unfocusedCaretColor)
        {
            this.unfocusedCaretColor = unfocusedCaretColor;
            return this;
        }

        public SearchInputWidget build()
        {
            return new SearchInputWidget(
                    x, y, //
                    width, height, isRounded, outlineThickness, //
                    backgroundColor, outlineColor, focusedCaretColor, unfocusedCaretColor
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }

    public enum SearchStatus
    {
        IDLE,
        SEARCHING
    }
}
