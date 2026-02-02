package com.drypted.spotlight.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.function.Consumer;

public class HotbarFocusProxyWidget extends AbstractWidget
{
    public final ArrayList<SearchHotbarWidget> searchHotbarWidgets;

    private Consumer<Boolean> onFocusChanged;

    public HotbarFocusProxyWidget(ArrayList<SearchHotbarWidget> searchHotbarWidgets)
    {
        super(0, 0, 0, 0, Component.empty());
        this.searchHotbarWidgets = searchHotbarWidgets;
    }

    public static HotbarFocusProxyWidget create(ArrayList<SearchHotbarWidget> searchHotbarWidgets)
    {
        return new HotbarFocusProxyWidget(searchHotbarWidgets);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) { }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        if (onFocusChanged != null)
        {
            onFocusChanged.accept(focused);
        }
        searchHotbarWidgets.forEach(w -> w.setShowBind(focused));
    }

    public void highlightSlot(int slotIndex)
    {
        searchHotbarWidgets.forEach(widget -> widget.setHighlighted(false));
        // get slot index and set show bind to true for that widget
        searchHotbarWidgets.stream()
                           .filter(widget -> widget.getHotbarIndex() == slotIndex)
                           .findFirst()
                           .ifPresent(widget -> widget.setHighlighted(true));
    }

    public void unhighlightAllSlots()
    {
        searchHotbarWidgets.forEach(widget -> widget.setHighlighted(false));
    }

    public void setOnFocusChanged(Consumer<Boolean> onFocusChanged)
    {
        this.onFocusChanged = onFocusChanged;
    }
}