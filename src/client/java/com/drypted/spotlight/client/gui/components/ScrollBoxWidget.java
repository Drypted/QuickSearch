package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.models.ScrollBoxWidgetEntry;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.gui.utils.renderer.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private int lastId = 0;
    private int selectedIndex = 0;

    private Consumer<Boolean> onFocus;

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

    /* Children */

    public int addChildRow(AbstractWidget widget)
    {
        int id = lastId++;
        this.children.add(new WidgetEntry(widget, id));
        return id;
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

    @Nullable
    public AbstractWidget getChildByIndex(int index)
    {
        if (index < 0 || index >= children.size())
            return null;
        return children.get(index).widget;
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

    public void removeAllChildren()
    {
        selectedIndex = -1;
        children.clear();
        setScrollAmount(0.0);
    }

    /* Meta (Scroll Logic) */

    private int contentHeight()
    {
        int height = margin;

        for (WidgetEntry e : children)
        {
            height += e.widget.getHeight() + spacing;
        }

        return height + margin;
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
        if (showScrollerAlways)
            return true;
        return (maxScrollAmount() > 0);
    }

    private int scrollerHeight()
    {
        final int viewportHeight = height;
        final int totalContentHeight = contentHeight();

        final float rawScrollerHeight = (float) (viewportHeight * viewportHeight) / (float) totalContentHeight;

        final int minScrollerHeight = 32;

        // max scroller height is viewport height
        return Mth.clamp((int) rawScrollerHeight, minScrollerHeight, viewportHeight);
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

    private void updateScrollPosition(double mouseY)
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
            final int scrollerHeight = scrollerHeight();
            final int trackHeight = height - scrollerHeight;

            // This centers the scroller thumb on the mouse cursor
            final double mouseOffset = mouseY - getY() - scrollerHeight / 2.0;
            final double scrollRatio = Mth.clamp(mouseOffset / trackHeight, 0.0, 1.0);

            setScrollAmount(scrollRatio * maxScrollAmount());
        }
    }

    /* Mouse Scroll Input */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        // Check scrollbar first
        if (scrollbarVisible() && isValidClickButton(button) && isOverScrollbar(mouseX, mouseY))
        {
            this.scrolling = true;

            updateScrollPosition(mouseY);

            return true;
        }

        // Check children (in reverse order for proper z-order)
        if (isMouseOver(mouseX, mouseY))
        {
            for (int i = children.size() - 1; i >= 0; i--)
            {
                AbstractWidget w = children.get(i).widget;
                if (w.mouseClicked(mouseX, mouseY, button))
                    return true;
            }
        }

        return false;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (!visible || !isMouseOver(mouseX, mouseY))
        {
            return false;
        }

        this.scrolling = true;

        setScrollAmount(scrollAmount - scrollY * scrollRate());
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (scrolling)
        {
            updateScrollPosition(mouseY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public int getMaxWidth()
    {
        return this.width - (margin * 2) - (scrollbarVisible() ? SCROLLBAR_WIDTH : 0);
    }

    /* Render */

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
        int yOffset = margin;

        for (WidgetEntry entry : children)
        {
            AbstractWidget widget = entry.widget;

            widget.setPosition(getX() + margin, getY() + yOffset - scroll);

            yOffset += widget.getHeight() + spacing;
        }
    }

    private void drawBackground(GuiGraphics g, int x1, int y1, int x2, int y2)
    {
        RenderUtils.drawRectangle(
                g,
                x1,
                y1,
                x2,
                y2,
                RoundedCorners.none(),
                1,
                true,
                bgColor,
                outlineColor
        );
    }

    private void drawChildren(GuiGraphics g, int mouseX, int mouseY, float delta, int x1, int y1, int x2, int y2)
    {
        // Clip
        g.enableScissor(x1 + 1, y1 + 1, x2 - 1, y2 - 1);

        for (WidgetEntry e : children)
        {
            AbstractWidget child = e.widget;
            if (!child.visible)
                continue;
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

    /* Select Element with keys */

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        if (onFocus != null)
        {
            onFocus.accept(focused);
        }

        // select
        if (selectedIndex < 0 && !children.isEmpty())
        {
            selectedIndex = 0;
        }
        // handles null state inside
        updateSelectionState(selectedIndex, focused);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (children.isEmpty())
            return super.keyPressed(keyCode, scanCode, modifiers);

        switch (keyCode)
        {
            case GLFW.GLFW_KEY_UP ->
            {
                unpressAllChildren();
                changeSelection(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN ->
            {
                unpressAllChildren();
                changeSelection(1);
                return true;
            }
            case GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER ->
            {
                if (getChildByIndex(selectedIndex) instanceof ScrollBoxWidgetEntry pressable)
                {
                    pressable.press();
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (children.isEmpty())
            return super.keyPressed(keyCode, scanCode, modifiers);

        // unpress all on any key release; if specified, it breaks when moving up and down while holding pressing key
        unpressAllChildren();

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void unpressAllChildren()
    {
        if (getChildByIndex(selectedIndex) instanceof ScrollBoxWidgetEntry pressable)
        {
            pressable.unpress();
        }
    }

    /**
     * Moves the selection by a relative amount (e.g., +1 or -1) and wraps if necessary.
     */
    private void changeSelection(int delta)
    {
        int oldIndex = selectedIndex;

        // Calculate new index with wrapping
        int newIndex = selectedIndex + delta;
        // wrap around
        if (newIndex < 0)
            newIndex = children.size() - 1;
        else if (newIndex >= children.size())
            newIndex = 0;

        if (oldIndex != newIndex)
        {
            updateSelectionState(oldIndex, false); // Deselect old
        }

        selectedIndex = newIndex;
        updateSelectionState(selectedIndex, true); // Select new
        scrollChildToView(selectedIndex);
    }

    /**
     * Updates a specific child's selection state if it implements ScrollBoxWidgetEntry.
     */
    private void updateSelectionState(int index, boolean isSelected)
    {
        AbstractWidget widget = getChildByIndex(index);
        if (widget instanceof ScrollBoxWidgetEntry selectable)
        {
            selectable.select(isSelected);
        }
    }

    private void scrollChildToView(int index)
    {
        final int paddingTop = 8, paddingBottom = 8;

        if (index < 0 || index >= children.size())
            return;

        // compute child top
        int childTop = margin;

        for (int i = 0; i < index; i++)
        {
            AbstractWidget w = children.get(i).widget;
            childTop += w.getHeight() + spacing;
        }

        AbstractWidget child = children.get(index).widget;
        int childBottom = childTop + child.getHeight();

        // viewport in content space
        int viewTop = (int) scrollAmount + paddingTop;
        int viewBottom = (int) scrollAmount + height - paddingBottom;

        // scroll moveUp
        if (childTop < viewTop)
        {
            setScrollAmount(childTop - paddingTop);
        }
        // scroll down
        else if (childBottom > viewBottom)
        {
            setScrollAmount(childBottom - height + paddingBottom);
        }
    }

    private void validateSelectedIndex()
    {
        if (selectedIndex < 0)
            selectedIndex = children.size() - 1;

        if (selectedIndex >= children.size())
            selectedIndex = 0;
    }


    /* Getters And Setters */

    public void setOnFocusCallback(Consumer<Boolean> onFocus)
    {
        this.onFocus = onFocus;
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
        private final int height;
        private int margin = 4;
        private int spacing = margin; // by default, spacing = margin
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

    private record WidgetEntry(AbstractWidget widget, int id)
    {
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            WidgetEntry other = (WidgetEntry) obj;
            return this.id == other.id;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration)
    {
    }
}
