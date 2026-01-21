package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import com.drypted.spotlight.client.utils.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScrollBoxWidget extends AbstractWidget
{
    private static final int SCROLLBAR_WIDTH = 6;

    private final List<WidgetEntry> children = new ArrayList<>();
    private final int margin;
    private final int spacing;
    private final boolean showScrollerAlways;
    private final Color bgColor;
    private final Color outlineColor;
    private final Color scrollbarColor;
    private final Color scrollerColor;

    private double scrollAmount;
    private boolean scrolling;

    public ScrollBoxWidget(int x, int y, int width, int height, int margin, int spacing, boolean showScrollerAlways, Color bgColor, Color outlineColor, Color scrollbarColor, Color scrollerColor)
    {
        super(x, y, width, height, Component.empty());
        this.margin = margin;
        this.showScrollerAlways = showScrollerAlways;
        this.bgColor = bgColor;
        this.spacing = spacing;
        this.outlineColor = outlineColor;
        this.scrollbarColor = scrollbarColor;
        this.scrollerColor = scrollerColor;
    }

    /* ---------------- Children ---------------- */

    public void addChildRow(AbstractWidget widget, int id)
    {
        int contentY = margin;
        if (!children.isEmpty())
        {
            WidgetEntry lastEntry = children.getLast();
            contentY = lastEntry.contentY + lastEntry.widget.getHeight() + spacing;
        }
        this.children.add(new WidgetEntry(widget, margin, contentY, id));
    }

    public void addChildAt(AbstractWidget widget, int contentX, int contentY, int id)
    {
        this.children.add(new WidgetEntry(widget, contentX, contentY, id));
    }

    @Nullable
    public AbstractWidget getChild(int id)
    {
        for (WidgetEntry entry : children)
        {
            if (entry.id == id)
            {
                return entry.widget;
            }
        }
        return null;
    }

    public List<AbstractWidget> getAllChildren()
    {
        List<AbstractWidget> widgets = new ArrayList<>();
        for (WidgetEntry entry : children)
        {
            widgets.add(entry.widget);
        }
        return widgets;
    }

    public void removeChild(AbstractWidget widget)
    {
        children.removeIf(entry -> entry.widget == widget);
    }

    /* ---------------- Scroll Logic ---------------- */

    private int contentHeight()
    {
        int max = 0;
        for (WidgetEntry e : children)
        {
            max = Math.max(max, e.contentY + e.widget.getHeight());
        }
        return max + margin;
    }

    private double scrollRate()
    {
        return 10.0;
    }

    private int maxScrollAmount()
    {
        return Math.max(0, contentHeight() - height);
    }

    private boolean scrollbarVisible()
    {
        if (showScrollerAlways) return true;
        return (maxScrollAmount() > 0);
    }

    private int scrollerHeight()
    {
        final int viewportHeight = height;
        final int totalContentHeight = contentHeight();

        final float rawScrollerHeight = (float) (viewportHeight * viewportHeight) / (float) totalContentHeight;

        final int minScrollerHeight = 32;
        final int maxScrollerHeight = viewportHeight - 8;

        return Mth.clamp((int) rawScrollerHeight, minScrollerHeight, maxScrollerHeight);
    }

    private int scrollBarX()
    {
        return getRight() - SCROLLBAR_WIDTH;
    }

    private int scrollBarY()
    {
        final int maxScrollAmount = maxScrollAmount();
        final int topY = getY();

        if (maxScrollAmount <= 0)
        {
            return topY;
        }

        final int availableTrackHeight = height - scrollerHeight();
        final double scrollRatio = scrollAmount / maxScrollAmount;
        final int scrollerOffset = (int) (scrollRatio * availableTrackHeight);

        return topY + scrollerOffset;
    }


    private void setScrollAmount(double amount)
    {
        this.scrollAmount = Mth.clamp(amount, 0.0, (double) maxScrollAmount());
    }

    private boolean isOverScrollbar(double mouseX, double mouseY)
    {
        return mouseX >= scrollBarX() && mouseX <= scrollBarX() + SCROLLBAR_WIDTH && mouseY >= getY() && mouseY < getBottom();
    }

    /* ---------------- Render ---------------- */

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta)
    {
        layoutChildren();

        final int x1 = getX();
        final int y1 = getY();
        final int x2 = x1 + width;
        final int y2 = y1 + height;

        drawBackground(g, x1, y1, x2, y2);
        drawChildren(g, mouseX, mouseY, delta, x1, y1, x2, y2);
        renderCustomScrollbar(g, mouseX, mouseY);
    }

    private void layoutChildren()
    {
        int scroll = (int) scrollAmount;

        for (WidgetEntry entry : children)
        {
            entry.widget.setPosition(getX() + entry.contentX, getY() + entry.contentY - scroll);
        }
    }

    private void drawBackground(GuiGraphics g, int x1, int y1, int x2, int y2)
    {
        // Background
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, bgColor.asInt());

        // Border
        g.fill(x1, y1, x2, y1 + 1, outlineColor.asInt());
        g.fill(x1, y2 - 1, x2, y2, outlineColor.asInt());
        g.fill(x1, y1, x1 + 1, y2, outlineColor.asInt());
        g.fill(x2 - 1, y1, x2, y2, outlineColor.asInt());
    }

    private void drawChildren(GuiGraphics g, int mouseX, int mouseY, float delta, int x1, int y1, int x2, int y2)
    {
        // Clip
        g.enableScissor(x1 + 1, y1 + 1, x2 - 1, y2 - 1);

        for (WidgetEntry e : children)
        {
            AbstractWidget child = e.widget;
            if (!child.visible) continue;
            child.render(g, mouseX, mouseY, delta);
        }

        g.disableScissor();
    }

    private void renderCustomScrollbar(GuiGraphics g, int mouseX, int mouseY)
    {
        if (!scrollbarVisible())
        {
            return;
        }

        int x = scrollBarX();
        int y = scrollBarY();
        int h = scrollerHeight();

        // scrollbar background
        g.fill(x, getY(), x + SCROLLBAR_WIDTH, getBottom(), scrollbarColor.asInt());
        // scroller
        g.fill(x, y, x + SCROLLBAR_WIDTH, y + h, scrollerColor.asInt());
        // line
        g.fill(x, getY(), x + 1, getBottom(), outlineColor.asInt());

        // unsupported cursor change
        // if (isOverScrollbar(mouseX, mouseY))
        // {
        //     g.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
        // }
    }

    /* ---------------- Input ---------------- */

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (!visible)
        {
            return false;
        }

        final double scrollDelta = scrollY * scrollRate();
        final double newScrollAmount = scrollAmount - scrollDelta;

        setScrollAmount(newScrollAmount);
        return true;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        // Check scrollbar first
        if (scrollbarVisible() && isValidClickButton(button) && isOverScrollbar(mouseX, mouseY))
        {
            this.scrolling = true;
            return true;
        }

        // Check children (in reverse order for proper z-order)
        for (int i = children.size() - 1; i >= 0; i--)
        {
            AbstractWidget w = children.get(i).widget;
            if (w.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (scrolling)
        {
            final int topY = getY();
            final int bottomY = getBottom();

            if (mouseY < topY)
            {
                setScrollAmount(0.0);
            }
            else if (mouseY > bottomY)
            {
                setScrollAmount(maxScrollAmount());
            }
            else
            {
                final double maxScroll = Math.max(1.0, maxScrollAmount());
                final int scrollerHeight = scrollerHeight();
                final int trackHeight = height - scrollerHeight;

                final double scrollScale = Math.max(1.0, maxScroll / trackHeight);
                final double newScrollAmount = scrollAmount + deltaY * scrollScale;

                setScrollAmount(newScrollAmount);
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        this.scrolling = false;

        for (WidgetEntry entry : children)
        {
            entry.widget.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration)
    {
    }

    public int getMaxWidth()
    {
        return this.width - (margin * 2) - (scrollbarVisible() ? SCROLLBAR_WIDTH : 0);
    }

    /* ---------------- Builder ---------------- */

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
        private int margin = 4;
        private int spacing = 0;
        private boolean showScrollerAlways = false;

        private Color bgColor = Colors.BLACK.withHalfAlpha();
        private Color outlineColor = Colors.WHITE;
        private Color scrollbarColor = Colors.CLEAR;
        private Color scrollerColor = Colors.WHITE;

        private Builder(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder margin(int margin)
        {
            this.margin = margin;
            return this;
        }

        public Builder spacing(int spacing)
        {
            this.spacing = spacing;
            return this;
        }

        public Builder showScrollerAlways(boolean showScrollerAlways)
        {
            this.showScrollerAlways = showScrollerAlways;
            return this;
        }

        public Builder bgColor(Color color)
        {
            this.bgColor = color;
            return this;
        }

        public Builder outlineColor(Color color)
        {
            this.outlineColor = color;
            return this;
        }

        public Builder scrollbarColor(Color color)
        {
            this.scrollbarColor = color;
            return this;
        }

        public Builder scrollerColor(Color color)
        {
            this.scrollerColor = color;
            return this;
        }

        public ScrollBoxWidget build()
        {
            return new ScrollBoxWidget(
                    x,
                    y,
                    width,
                    height,
                    margin,
                    spacing,
                    showScrollerAlways,
                    bgColor,
                    outlineColor,
                    scrollbarColor,
                    scrollerColor
            );
        }
    }

    private record WidgetEntry(AbstractWidget widget, int contentX, int contentY, int id)
    {
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WidgetEntry other = (WidgetEntry) obj;
            return this.id == other.id;
        }
    }
}