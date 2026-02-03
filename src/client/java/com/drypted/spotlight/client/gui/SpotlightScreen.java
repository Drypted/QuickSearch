package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.SearchHandler;
import com.drypted.spotlight.client.models.SearchResultData;
import com.drypted.spotlight.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SpotlightScreen extends Screen
{
    private static final int SEARCH_BAR_WIDTH = 200;
    private static final int SEARCH_BAR_HEIGHT = 22;

    private static final int DISTANCE_FROM_CENTER = 20;
    private static final int RESULTS_HEIGHT = 100;

    private static final int HOTBAR_SLOTS = 9;

    private InputWidget inputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private HotbarWidget hotbarFocusProxy;

    public SpotlightScreen()
    {
        super(Component.literal("Spotlight Menu"));
    }

    @Override
    protected void init()
    {
        final int searchBarX = (this.width - SEARCH_BAR_WIDTH) / 2;
        final int searchBarY = (this.height - SEARCH_BAR_HEIGHT) / 2 - DISTANCE_FROM_CENTER;

        this.inputWidget = InputWidget.builder(
                searchBarX,
                searchBarY,
                SEARCH_BAR_WIDTH,
                SEARCH_BAR_HEIGHT
        ).build();
        this.inputWidget.setPlaceholder("Search items for blocks ...");
        // set validator for no symbols
        this.inputWidget.setValidator(text -> text.matches("[a-zA-Z0-9 _-]*"));

        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                inputWidget.getY() + SEARCH_BAR_HEIGHT - inputWidget.getOutlineThickness(),
                inputWidget.getWidth(),
                RESULTS_HEIGHT
        ).showScrollerAlways(true).build();

        this.inputWidget.addTextChangeListener(this::onType);

        this.hotbarFocusProxy = HotbarWidget.create(
                SEARCH_BAR_WIDTH,
                searchBarX,
                searchBarY,
                inputWidget
        );

        this.addRenderableWidget(hotbarFocusProxy);
        this.addRenderableWidget(searchResultsWidget);
        this.addRenderableWidget(inputWidget);

        // show search on open
        setVisible(this.searchResultsWidget, false);
        setVisible(this.hotbarFocusProxy, false);
        this.hotbarFocusProxy.getWidgets().forEach(widget -> setVisible(widget, false));

        this.setFocused(inputWidget);
    }

    private void displayResults(List<SearchResultData> results)
    {
        clearResults();

        // If results came back empty (or query was canceled/cleared mid-flight), stop here.
        if (results.isEmpty())
        {
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            return;
        }

        int matchCount = 0;
        for (SearchResultData result : results)
        {
            // fill hotbar for first 9
            if (matchCount < HOTBAR_SLOTS)
            {
                HotbarSlotWidget widget = this.hotbarFocusProxy.getWidgets().get(matchCount);
                if (widget != null)
                {
                    widget.setSearchResultData(result);
                    widget.setOnClickCallback(mouseButtonClick -> {
                        onResultClicked(result);
                        widget.setFocused(true);
                    });
                }
            }

            this.searchResultsWidget.addChildRow( //
                    SearchResultDataWidget.builder(0, 0, result)
                                          .width(searchResultsWidget.getMaxWidth())
                                          .onClick((mBC, dC) -> onResultClicked(result))
                                          .build() //
            );

            matchCount++;
        }

        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    private void clearResults()
    {
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
        this.searchResultsWidget.removeAllChildren();
        hotbarFocusProxy.getWidgets().forEach(widget -> widget.setSearchResultData(null));
    }

    private void onResultClicked(SearchResultData block)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
            player.connection.sendCommand(block.getCommandString());
    }

    /* Input */

    private void onType(String text)
    {
        if (text == null || text.isEmpty())
        {
            this.hotbarFocusProxy.getWidgets().forEach(widget -> setVisible(widget, false));
            setVisible(this.searchResultsWidget, false);
            setVisible(this.hotbarFocusProxy, false);
            clearResults();
            return;
        }

        // Set visual state to searching
        inputWidget.setSearchStatus(InputWidget.SearchStatus.SEARCHING);
        this.hotbarFocusProxy.getWidgets().forEach(widget -> setVisible(widget, true));
        setVisible(this.searchResultsWidget, true);
        setVisible(this.hotbarFocusProxy, true);

        // Delegate logic to Handler
        SearchHandler.searchAsync(text, this::displayResults);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (SpotlightEntryClient.closeSpotlightKeyMapping.matches(keyCode, scanCode))
        {
            if (inputWidget.isFocused() && inputWidget.hasText())
            {
                inputWidget.clearText();
                clearResults();
                this.hotbarFocusProxy.getWidgets().forEach(widget -> setVisible(widget, false));
                setVisible(this.searchResultsWidget, false);
                setVisible(this.hotbarFocusProxy, false);
                return true;
            }
            this.onClose();
            return true;
        }

        if (this.minecraft == null)
            return super.keyPressed(keyCode, scanCode, modifiers);

        // only allow key when hotbar is focused
        if (hotbarFocusProxy.isFocused())
        {
            for (int i = 0; i < HOTBAR_SLOTS; i++)
            {
                HotbarSlotWidget hotbarWidget = hotbarFocusProxy.getWidgets().get(i);
                if (hotbarWidget != null && keyCode == this.minecraft.options.keyHotbarSlots[i].getDefaultKey()
                                                                                               .getValue())
                {
                    hotbarFocusProxy.onHotbarKeyPressed(hotbarWidget, modifiers);
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!super.mouseClicked(mouseX, mouseY, button))
        {
            // click outside, close spotlight
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /* Overrides for settings */

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) { }

    @Override
    public boolean isPauseScreen() { return false; }

    /* Helpers */

    public void setVisible(AbstractWidget widget, boolean visible)
    {
        widget.visible = visible;
        widget.active = visible;
    }

    @Override
    protected void changeFocus(ComponentPath componentPath)
    {
        super.changeFocus(componentPath);
    }
}