package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.SearchHandler;
import com.drypted.spotlight.client.core.models.SearchResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;

public class SpotlightScreen extends Screen
{
    private static final int SEARCH_BAR_WIDTH = 200;
    private static final int SEARCH_BAR_HEIGHT = 20;

    private static final int HOTBAR_SLOT_PADDING = 2;

    private static final int DISTANCE_FROM_CENTER = 20;
    private static final int RESULTS_HEIGHT = 100;
    private static final int HOTBAR_SLOTS = 9;

    private SearchInputWidget searchInputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private final HashMap<Integer, SearchHotbarWidget> searchHotbarWidgets = new HashMap<>();

    public SpotlightScreen()
    {
        super(Component.literal("Spotlight Menu"));
    }

    @Override
    protected void init()
    {
        final int searchBarX = (this.width - SEARCH_BAR_WIDTH) / 2;
        final int searchBarY = (this.height - SEARCH_BAR_HEIGHT) / 2 - DISTANCE_FROM_CENTER;

        this.searchInputWidget = SearchInputWidget.builder(
                searchBarX,
                searchBarY,
                SEARCH_BAR_WIDTH,
                SEARCH_BAR_HEIGHT
        ).build();

        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                searchInputWidget.getY() + SEARCH_BAR_HEIGHT - searchInputWidget.getOutlineThickness(),
                searchInputWidget.getWidth(),
                RESULTS_HEIGHT
        ).showScrollerAlways(true).build();

        generateHotbarWidgets(searchBarY, searchBarX);

        this.searchInputWidget.subscribeToTypeCallback(this::onType);

        this.searchHotbarWidgets.values().forEach(this::addRenderableWidget);
        this.addRenderableWidget(searchInputWidget);
        this.addRenderableWidget(searchResultsWidget);

        // show search on open
        this.searchResultsWidget.visible = false;
        this.searchHotbarWidgets.values().forEach(widget -> widget.visible = false);
        this.setFocused(searchInputWidget);
    }

    private void generateHotbarWidgets(int searchBarY, int searchBarX)
    {
        final float iconSize = (SEARCH_BAR_WIDTH - HOTBAR_SLOT_PADDING * (HOTBAR_SLOTS + 1)) / (float) HOTBAR_SLOTS;
        final int endY = searchBarY - searchInputWidget.getOutlineThickness() - HOTBAR_SLOT_PADDING;
        final int startY = (int) Math.ceil(endY - iconSize);

        float cursor = searchBarX + HOTBAR_SLOT_PADDING;
        for (int i = 0; i < HOTBAR_SLOTS; i++)
        {
            this.searchHotbarWidgets.put(
                    i, SearchHotbarWidget.builder(
                            (int) Math.ceil(cursor),
                            startY,
                            (int) Math.ceil(iconSize),
                            (int) Math.ceil(iconSize)
                    ).build()
            );
            cursor += iconSize + HOTBAR_SLOT_PADDING;
        }
    }

    /* Callbacks */

    private void onType(String text)
    {
        if (text == null || text.isEmpty())
        {
            this.searchHotbarWidgets.values().forEach(widget -> widget.visible = false);
            this.searchResultsWidget.visible = false;
            clearResults();
            return;
        }

        // Set visual state to searching
        searchInputWidget.setSearchStatus(SearchInputWidget.SearchStatus.SEARCHING);
        this.searchHotbarWidgets.values().forEach(widget -> widget.visible = true);
        this.searchResultsWidget.visible = true;

        // Delegate logic to Handler
        SearchHandler.searchAsync(text, this::displayResults);
    }

    private void displayResults(List<SearchResultData> results)
    {
        clearResults();

        // If results came back empty (or query was cancelled/cleared mid-flight), stop here.
        if (results.isEmpty())
        {
            searchInputWidget.setSearchStatus(SearchInputWidget.SearchStatus.IDLE);
            return;
        }

        int matchCount = 0;
        for (SearchResultData result : results)
        {
            // fill hotbar for first 9
            if (matchCount < HOTBAR_SLOTS)
            {
                SearchHotbarWidget widget = searchHotbarWidgets.get(matchCount);
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
                    SearchResultsWidget.builder(0, 0, result)
                                       .width(searchResultsWidget.getMaxWidth())
                                       .onClick((mBC, dC) -> {
                                           onResultClicked(result);
                                       })
                                       .build() //
            );

            matchCount++;
        }

        searchInputWidget.setSearchStatus(SearchInputWidget.SearchStatus.IDLE);
    }

    private void clearResults()
    {
        searchInputWidget.setSearchStatus(SearchInputWidget.SearchStatus.IDLE);
        this.searchResultsWidget.removeAllChildren();
        searchHotbarWidgets.values().forEach(widget -> widget.setSearchResultData(null));
    }

    private void onResultClicked(SearchResultData block)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
            player.connection.sendCommand(block.getCommandString());
    }

    /* Input */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (SpotlightEntryClient.closeSpotlightKeyMapping.matches(keyCode, scanCode))
        {
            if (searchInputWidget.hasText())
            {
                searchInputWidget.clearText();
                clearResults();
                return true;
            }
            this.onClose();
            return true;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return super.keyPressed(keyCode, scanCode, modifiers);

        for (int i = 0; i < HOTBAR_SLOTS; i++)
        {
            if (keyCode == Minecraft.getInstance().options.keyHotbarSlots[i].getDefaultKey()
                                                                            .getValue())
            {
                SearchHotbarWidget hotbarWidget = searchHotbarWidgets.get(i);
                if (hotbarWidget != null && hotbarWidget.getSearchResultData() != null)
                {
                    hotbarWidget.setFocused(true);
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) { }

    @Override
    public boolean isPauseScreen() { return false; }
}