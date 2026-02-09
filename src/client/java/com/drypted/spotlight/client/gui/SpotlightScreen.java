package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.functions.Actions;
import com.drypted.spotlight.client.core.search.SearchHandler;
import com.drypted.spotlight.client.gui.components.*;
import com.drypted.spotlight.client.models.SearchResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpotlightScreen extends Screen
{
    // private static final int searchBarWidth = 200;
    private static final int SEARCH_BAR_HEIGHT = 22;

    private static final int DISTANCE_FROM_CENTER = 20;
    // private static final int resultsHeight = 100;

    private static final int HOTBAR_SLOTS = 9;

    private InputWidget inputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private @Nullable HotbarWidget hotbarWidget;

    public SpotlightScreen()
    {
        super(Component.literal("Spotlight Menu"));
    }

    @Override
    protected void init()
    {
        final int searchBarWidth = SpotlightEntryClient.getConfig().searchBarWidth;
        final int resultsHeight = SpotlightEntryClient.getConfig().resultsBoxHeight;

        final int searchBarX = (this.width - searchBarWidth) / 2;
        final int searchBarY = (this.height - SEARCH_BAR_HEIGHT) / 2 - DISTANCE_FROM_CENTER;

        this.inputWidget = InputWidget.builder(
                searchBarX,
                searchBarY,
                searchBarWidth,
                SEARCH_BAR_HEIGHT
        ).build();
        this.inputWidget.setPlaceholder("Search items for blocks ...");
        // set validator for no symbols
        this.inputWidget.setValidator(text -> text.matches("[a-zA-Z0-9 _-]*"));

        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                inputWidget.getY() + SEARCH_BAR_HEIGHT - inputWidget.getOutlineThickness(),
                inputWidget.getWidth(),
                resultsHeight
        ).showScrollerAlways(true).build();

        this.inputWidget.addTextChangeListener(this::onTextChanged);

        if (isHotbarEnabledInConfig())
        {
            this.hotbarWidget = HotbarWidget.create(
                    searchBarX,
                    searchBarWidth,
                    searchBarY - HotbarWidget.HOTBAR_SLOT_PADDING
            );
            this.addRenderableWidget(hotbarWidget);
        }

        this.addRenderableWidget(searchResultsWidget);
        this.addRenderableWidget(inputWidget);

        // show search on open
        setResultsVisible(false);

        this.setFocused(inputWidget);
    }

    /* Input */

    private void onTextChanged(String text)
    {
        if (text == null || text.isEmpty())
        {
            setResultsVisible(false);
            clearResults();
            return;
        }

        // Set visual state to searching
        inputWidget.setSearchStatus(InputWidget.SearchStatus.SEARCHING);
        setResultsVisible(true);

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
                setResultsVisible(false);
                return true;
            }
            this.onClose();
            return true;
        }

        if (this.minecraft == null)
            return super.keyPressed(keyCode, scanCode, modifiers);

        // only allow key when hotbar is focused
        if (isHotbarEnabledInConfig() && this.hotbarWidget != null && hotbarWidget.isFocused())
        {
            for (int i = 0; i < HOTBAR_SLOTS; i++)
            {
                HotbarSlotWidget hotbarWidget = this.hotbarWidget.getWidgets().get(i);
                if (hotbarWidget != null && keyCode == this.minecraft.options.keyHotbarSlots[i].getDefaultKey()
                                                                                               .getValue())
                {
                    this.hotbarWidget.onHotbarKeyPressed(hotbarWidget, modifiers);
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (isHotbarEnabledInConfig() && this.hotbarWidget != null)
            this.hotbarWidget.onAnyKeyReleased();

        return super.keyReleased(keyCode, scanCode, modifiers);
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

    /* Results */

    private void displayResults(List<SearchResultData> results)
    {
        clearResults();

        // If results came back empty (or query was canceled/cleared mid-flight), stop here.
        if (results.isEmpty())
        {
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            setResultsVisible(false);
            return;
        }

        int matchCount = 0;
        for (SearchResultData result : results)
        {
            // fill hotbar for first 9
            if (isHotbarEnabledInConfig() && this.hotbarWidget != null && matchCount < HOTBAR_SLOTS)
            {
                HotbarSlotWidget widget = this.hotbarWidget.getWidgets().get(matchCount);
                if (widget != null)
                {
                    widget.setSearchResultData(result);
                    widget.onClick(mouseButtonClick -> {
                        onResultClicked(result);
                        widget.setFocused(true);
                    });
                }
            }

            this.searchResultsWidget.addChildRow( //
                    SearchResultDataWidget.builder(0, 0, result)
                                          .width(searchResultsWidget.getMaxWidth())
                                          .onClick((mBC, dC) -> onResultClicked(result)).build() //
            );

            matchCount++;
        }

        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    private void onResultClicked(SearchResultData data)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
            Actions.giveItem(player, data);
    }

    /* Overrides for settings */

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) { } // don't render any background

    @Override
    public boolean isPauseScreen() { return false; }

    /* Helpers */

    public void setVisible(AbstractWidget widget, boolean visible)
    {
        if (widget == null)
            return;

        widget.visible = visible;
        widget.active = visible;
    }

    private void setResultsVisible(boolean visible)
    {
        if (isHotbarEnabledInConfig() && this.hotbarWidget != null)
        {
            this.hotbarWidget.getWidgets().forEach(widget -> setVisible(widget, visible));
            setVisible(this.hotbarWidget, visible);
        }

        setVisible(this.searchResultsWidget, visible);
    }

    private void clearResults()
    {
        this.inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
        this.searchResultsWidget.removeAllChildren();
        if (isHotbarEnabledInConfig() && this.hotbarWidget != null)
            this.hotbarWidget.getWidgets().forEach(widget -> widget.setSearchResultData(null));
    }

    private boolean isHotbarEnabledInConfig()
    {
        return SpotlightEntryClient.getConfig().showHotbarSlots;
    }
}