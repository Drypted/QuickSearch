package com.drypted.spotlight.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static com.drypted.spotlight.client.SpotlightEntryClient.categoryKeyBinding;

public class SpotlightScreen extends Screen
{
    private static final int DISTANCE_FRON_CENTER = 20;

    private SearchBarWidget searchBarWidget;

    public SpotlightScreen()
    {
        super(Component.literal("Spotlight Menu"));
    }

    @Override
    protected void init()
    {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = (this.width - buttonWidth) / 2;
        int y = (this.height - buttonHeight) / 2 - DISTANCE_FRON_CENTER;

        searchBarWidget = SearchBarWidget.builder(x, y, buttonWidth, buttonHeight).build();

        this.addRenderableWidget(searchBarWidget);

        this.setFocused(searchBarWidget);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f)
    {
        // no background rendering
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (categoryKeyBinding.matches(keyCode, scanCode))
        {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        if (!searchBarWidget.isMouseOver(d, e))
            this.onClose(); // close on any mouse click outside of search bar
        return super.mouseClicked(d, e, i);
    }
}