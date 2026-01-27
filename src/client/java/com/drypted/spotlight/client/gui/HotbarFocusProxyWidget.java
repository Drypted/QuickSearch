package com.drypted.spotlight.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class HotbarFocusProxyWidget extends AbstractWidget
{
    public final ArrayList<SearchHotbarWidget> searchHotbarWidgets;

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
        searchHotbarWidgets.forEach(w -> w.setShowBind(focused));
    }
}