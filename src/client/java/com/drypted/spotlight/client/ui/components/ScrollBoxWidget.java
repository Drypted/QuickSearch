package com.drypted.spotlight.client.ui.components;

import com.drypted.spotlight.client.core.blueprints.ui.ScrollBoxWidgetEntry;
import com.drypted.spotlight.client.core.blueprints.ui.common.Color;
import com.drypted.spotlight.client.core.blueprints.ui.common.Colors;
import com.drypted.spotlight.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.spotlight.client.ui.renderer.RenderCommon;
import com.drypted.spotlight.client.ui.styling.Styles;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrollBoxWidget extends AbstractWidget
{
    private static final int SCROLLBAR_WIDTH = 6;

    private RoundedCorners rounded;

    private final List<WidgetEntry> children = new ArrayList<>();
    private final int margin;
    private final int spacing;
    private final float outlineThickness;
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

    public ScrollBoxWidget(int x, int y, int width, int height, int margin, int spacing, float outlineThickness, RoundedCorners rounded, boolean showScrollerAlways, Color bgColor, Color outlineColor, Color scrollbarColor, Color scrollerColor)
    {
        super(x, y, width, height, Component.empty());
        this.margin = margin;
        this.outlineThickness = outlineThickness;
        this.showScrollerAlways = showScrollerAlways;
        this.rounded = rounded;
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
        if (index < 0 || index >= children.size()) return null;
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
        // outlineThickness is included on both top and bottom, mirroring how layoutChildren positions children
        int height = (int) (margin + outlineThickness);

        for (int i = 0; i < children.size(); i++)
        {
            height += children.get(i).widget.getHeight();
            // spacing is between items only, not after the last one
            if (i < children.size() - 1) height += spacing;
        }

        return (int) (height + margin + outlineThickness);
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

    private float scrollerHeight()
    {
        // viewport and content are both measured in the inner space (inside the outline border)
        final float viewportHeight = height - outlineThickness * 2;
        final int totalContentHeight = contentHeight();

        final float rawScrollerHeight = (viewportHeight * viewportHeight) / (float) totalContentHeight;

        final int minScrollerHeight = 32;

        // max scroller height is viewport height
        return Mth.clamp(rawScrollerHeight, minScrollerHeight, viewportHeight);
    }

    private float scrollBarX()
    {
        // inset by outlineThickness so the scrollbar sits inside the border, not on top of it
        return getRight() - outlineThickness - SCROLLBAR_WIDTH;
    }

    private float scrollBarY()
    {
        final int maxScrollAmount = maxScrollAmount();
        // scrollbar track starts inside the outline border
        final float topY = getY() + outlineThickness;

        if (maxScrollAmount <= 0)
        {
            return topY;
        }

        final float availableTrackHeight = (height - outlineThickness * 2) - scrollerHeight();
        final double scrollRatio = scrollAmount / maxScrollAmount;
        final double scrollerOffset = scrollRatio * availableTrackHeight;

        return (float) (topY + scrollerOffset);
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
            final float scrollerHeight = scrollerHeight();
            // track height is the inner viewport height (inside outline), not the full widget height
            final int trackHeight = (int) ((height - outlineThickness * 2) - scrollerHeight);

            // This centers the scroller thumb on the mouse cursor
            final double mouseOffset = mouseY - getY() - outlineThickness - scrollerHeight / 2.0;
            final double scrollRatio = Mth.clamp(mouseOffset / trackHeight, 0.0, 1.0);

            setScrollAmount(scrollRatio * maxScrollAmount());
        }
    }

    /* Mouse Scroll Input */

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mEv, boolean doubleClick)
    {
        // Check scrollbar first
        if (scrollbarVisible() && isValidClickButton(mEv.buttonInfo()) && isOverScrollbar(mEv.x(), mEv.y()))
        {
            this.scrolling = true;

            updateScrollPosition(mEv.y());

            return true;
        }

        // Check children (in reverse order for proper z-order)
        if (isMouseOver(mEv.x(), mEv.y()))
        {
            for (int i = children.size() - 1; i >= 0; i--)
            {
                AbstractWidget w = children.get(i).widget;
                if (w.mouseClicked(mEv, doubleClick)) return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent mEv)
    {
        this.scrolling = false;

        for (WidgetEntry entry : new ArrayList<>(children))
        {
            entry.widget.mouseReleased(mEv);
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
    public boolean mouseDragged(@NonNull MouseButtonEvent mEv, double deltaX, double deltaY)
    {
        if (scrolling)
        {
            updateScrollPosition(mEv.y());
            return true;
        }

        return super.mouseDragged(mEv, deltaX, deltaY);
    }

    public int getChildWidth()
    {
        // left inset:  outlineThickness + margin
        // right inset: outlineThickness + margin + scrollbar (when visible); scrollbar itself sits inset by outlineThickness
        int inset = (int) ((margin + outlineThickness) * 2);
        if (scrollbarVisible()) inset += SCROLLBAR_WIDTH;
        return this.width - inset;
    }

    /* Render */

    @Override
    protected void renderWidget(@NonNull GuiGraphics g, int mouseX, int mouseY, float delta)
    {
        layoutChildren();

        final int x1 = getX();
        final int y1 = getY();
        final int x2 = x1 + width;
        final int y2 = y1 + height;

        drawBackground(g, x1, y1, x2, y2);
        drawChildren(g, mouseX, mouseY, delta, x1, y1, x2, y2);
        renderCustomScrollbar(g);

        if (isOverScrollbar(mouseX, mouseY))
        {
            g.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
        }
    }

    private void layoutChildren()
    {
        int scroll = (int) scrollAmount;
        // children start after both the outline border and the margin on the top
        int yOffset = (int) (outlineThickness + margin);

        for (WidgetEntry entry : children)
        {
            AbstractWidget widget = entry.widget;

            // x: respect outline border + margin; keep children clear of the scrollbar on the right
            widget.setPosition((int) (getX() + outlineThickness + margin), getY() + yOffset - scroll);
            widget.setWidth(getChildWidth());

            yOffset += widget.getHeight() + spacing;
        }
    }

    private void drawBackground(GuiGraphics g, int x1, int y1, int x2, int y2)
    {
        RenderCommon.drawRectangle(g, x1, y1, x2, y2, rounded, outlineThickness, true, bgColor, outlineColor);
    }

    private void drawChildren(GuiGraphics g, int mouseX, int mouseY, float delta, int x1, int y1, int x2, int y2)
    {
        // Clip to the inner content area, keeping children behind the outline and clear of the scrollbar
        g.enableScissor(
                (int) (x1 + outlineThickness),
                (int) (y1 + outlineThickness),
                (int) (x2 - outlineThickness - (scrollbarVisible() ? SCROLLBAR_WIDTH : 0)),
                (int) (y2 - outlineThickness)
        );

        for (WidgetEntry e : children)
        {
            AbstractWidget child = e.widget;
            if (!child.visible) continue;
            child.render(g, mouseX, mouseY, delta);
        }

        g.disableScissor();
    }

    private void renderCustomScrollbar(GuiGraphics g)
    {
        if (!scrollbarVisible())
        {
            return;
        }

        // scrollbar background
        RenderCommon.drawRectangle(
                g,
                scrollBarX(),
                getY() + outlineThickness,
                scrollBarX() + SCROLLBAR_WIDTH,
                getBottom() - outlineThickness,
                RoundedCorners.none(),
                0,
                false,
                scrollbarColor,
                outlineColor
        );
        // scroller
        RenderCommon.drawRectangle(
                g,
                scrollBarX(),
                scrollBarY(),
                scrollBarX() + SCROLLBAR_WIDTH,
                scrollBarY() + scrollerHeight(),
                RoundedCorners.none(),
                outlineThickness,
                true,
                scrollerColor,
                outlineColor
        );
        // line
        RenderCommon.drawRectangle(
                g,
                scrollBarX(),
                getY() + outlineThickness,
                scrollBarX() + outlineThickness,
                getBottom() - outlineThickness,
                RoundedCorners.none(),
                0,
                false,
                outlineColor,
                Colors.CLEAR
        );
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
    public boolean keyPressed(@NonNull KeyEvent kEv)
    {
        if (children.isEmpty()) return super.keyPressed(kEv);

        switch (kEv.key())
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

        return super.keyPressed(kEv);
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent kEv)
    {
        if (children.isEmpty()) return super.keyPressed(kEv);

        // unpress all on any key release; if specified, it breaks when moving up and down while holding pressing key
        unpressAllChildren();

        return super.keyReleased(kEv);
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
        if (newIndex < 0) newIndex = children.size() - 1;
        else if (newIndex >= children.size()) newIndex = 0;

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

        if (index < 0 || index >= children.size()) return;

        // compute child top in content space; must match the layout origin used in layoutChildren
        int childTop = (int) (outlineThickness + margin);

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
        if (selectedIndex < 0) selectedIndex = children.size() - 1;

        if (selectedIndex >= children.size()) selectedIndex = 0;
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
        private int margin = 3;
        private int spacing = margin; // by default, spacing = margin
        private float outlineThickness = Styles.ScrollBox.OUTLINE_THICKNESS;
        private RoundedCorners rounded = RoundedCorners.fromVerticalSides(false, true);
        private boolean showScrollerAlways = false;

        private Color bgColor = Styles.ScrollBox.BACKGROUND_COLOR;
        private Color outlineColor = Styles.ScrollBox.OUTLINE_COLOR;
        private Color scrollbarColor = Styles.ScrollBox.SCROLLBAR_COLOR;
        private Color scrollerColor = Styles.ScrollBox.SCROLLER_COLOR;

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

        public Builder outlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return this;
        }


        public Builder rounded(RoundedCorners rounded)
        {
            this.rounded = rounded;
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
                    outlineThickness,
                    rounded,
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
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WidgetEntry other = (WidgetEntry) obj;
            return this.id == other.id;
        }
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narration)
    {
    }
}