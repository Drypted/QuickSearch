package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.BiConsumer;

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
    /// Padding between hotbar slots above the search box
    private static final int HOTBAR_PADDING = 4;

    private final int TextX;
    private final int TextY;

    private int searchBoxHeight;
    private int resultsBoxHeight;
    private final ScrollBoxWidget resultsWidget;


    private static final List<SearchResultData> SEARCH_DATA;

    static
    {
        SEARCH_DATA = BuiltInRegistries.ITEM.stream().map(SearchResultData::fromItem).toList();
    }

    public SearchBarWidget(int x, int y, int width, int searchBoxHeight, int resultsBoxHeight, boolean isRounded, int outlineThickness, Color backgroundColor, Color outlineColor)
    {
        super(x, y, width, searchBoxHeight + resultsBoxHeight, Component.empty());
        this.searchBoxHeight = searchBoxHeight;
        this.resultsBoxHeight = resultsBoxHeight;
        this.isRounded = isRounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;

        // calculate text position
        TextX = this.getX() + TEXT_PADDING_X;
        TextY = this.getY() + (searchBoxHeight - FONT.lineHeight) / 2;

        // create results box
        resultsWidget = ScrollBoxWidget.builder(
                        this.getX(),
                        this.getY() + searchBoxHeight - outlineThickness,
                        this.getWidth(),
                        resultsBoxHeight
                )
                .showScrollerAlways(true)
                .build();

        refreshResults();
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
                this.getY() + searchBoxHeight,
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

        // hotbar items
        drawHotbarSlots(guiGraphics);

        // render results box
        this.resultsWidget.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawHotbarSlots(GuiGraphics g)
    {
        final int slots = 9;

        // width = slots * iconSize + (slots + 1) * padding
        // => iconSize = (width - (slots + 1) * padding) / slots
        final float iconSize = (this.getWidth() - HOTBAR_PADDING * (slots + 1)) / (float) slots;

        final int endY = this.getY() + outlineThickness;
        final int startY = (int) Math.ceil(endY - iconSize);

        float cursor = this.getX() + HOTBAR_PADDING;
        for (int i = 0; i < slots; i++)
        {
            RenderUtils.fillRectangle(
                    g,
                    (int) Math.ceil(cursor),
                    startY,
                    (int) Math.ceil(cursor + iconSize),
                    endY,
                    true,
                    1,
                    true,
                    backgroundColor,
                    outlineColor
            );

            cursor += iconSize + HOTBAR_PADDING;
        }
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

    private void refreshResults()
    {
        this.resultsWidget.removeAllChildren();

        String searchText = this.text.toLowerCase();

        if (searchText.isEmpty())
        {
            return;
        }

        for (SearchResultData result : SEARCH_DATA)
        {
            if (result.getName().contains(searchText) || result.getIdentifier().contains(searchText))
            {
                this.resultsWidget.addChildRow(SearchResultWidget.builder(0, 0, result)
                                                       .width(resultsWidget.getMaxWidth())
                                                       .onClick(onResultClicked(result))
                                                       .build());
            }
        }
    }

    private BiConsumer<MouseButtonClick, Boolean> onResultClicked(SearchResultData block)
    {
        return (mouseButtonClick, isDoubleClick) -> {
            // give the player the result they clicked on
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            player.connection.sendCommand(block.getCommandString());
        };
    }

    /* Meta (Stuff to make it functional) */

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!this.canType) return false;

        if (StringUtil.isAllowedChatCharacter(codePoint))
        {
            this.text += codePoint;
            refreshResults();
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
            refreshResults();
            return true;
        }

        if (this.canType)
        {
            // erase on backspace
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.text.isEmpty())
            {
                this.text = this.text.substring(0, this.text.length() - 1);
                refreshResults();
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

    public int getSearchBoxHeight()
    {
        return searchBoxHeight;
    }

    public void setSearchBoxHeight(int searchBoxHeight)
    {
        this.searchBoxHeight = searchBoxHeight;
    }

    public int getResultsBoxHeight()
    {
        return resultsBoxHeight;
    }

    public void setResultsBoxHeight(int resultsBoxHeight)
    {
        this.resultsBoxHeight = resultsBoxHeight;
    }

    public String getText()
    {
        return text;
    }

    /* Forwarding events */
    @Override
    public boolean mouseScrolled(double d, double e, double f, double g)
    {
        if (resultsWidget.mouseScrolled(d, e, f, g))
        {
            return true;
        }
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        if (resultsWidget.mouseClicked(d, e, i))
        {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i)
    {
        if (resultsWidget.mouseReleased(d, e, i))
        {
            return true;
        }
        return super.mouseReleased(d, e, i);
    }

    /* Builder */

    public static Builder builder(int x, int y, int width, int searchBoxHeight)
    {
        return new Builder(x, y, width, searchBoxHeight);
    }

    public static Builder builder(int x, int y, int width, int searchBoxHeight, int resultsBoxHeight)
    {
        return new Builder(x, y, width, searchBoxHeight, resultsBoxHeight);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private final int width;
        private int searchBoxHeight;
        private int resultsBoxHeight = 100;
        private boolean isRounded = false;
        private int outlineThickness = 1;
        private Color backgroundColor = Colors.BLACK.withHalfAlpha();
        private Color outlineColor = Colors.WHITE;


        public Builder(int x, int y, int width, int searchBoxHeight, int resultsBoxHeight)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.searchBoxHeight = searchBoxHeight;
            this.resultsBoxHeight = resultsBoxHeight;
        }

        public Builder(int x, int y, int width, int searchBoxHeight)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.searchBoxHeight = searchBoxHeight;
        }

        public Builder searchBoxHeight(int searchBoxHeight)
        {
            this.searchBoxHeight = searchBoxHeight;
            return this;
        }

        public Builder resultsBoxHeight(int resultsBoxHeight)
        {
            this.resultsBoxHeight = resultsBoxHeight;
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

        public SearchBarWidget build()
        {
            return new SearchBarWidget(
                    x,
                    y,
                    width,
                    searchBoxHeight,
                    resultsBoxHeight,
                    isRounded,
                    outlineThickness,
                    backgroundColor,
                    outlineColor
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
