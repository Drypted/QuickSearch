package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiConsumer;

import static com.drypted.spotlight.client.SpotlightEntryClient.closeSpotlightKeyMapping;

public class SpotlightScreen extends Screen
{
    private static final int SEARCH_BAR_WIDTH = 200;
    private static final int SEARCH_BAR_HEIGHT = 20;

    private static final int DISTANCE_FRON_CENTER = 20;
    private static final int HOTBAR_OFFSET = 20;
    private static final int RESULTS_HEIGHT = 100;

    private SearchHotbarWidget searchHotbarWidget;
    private SearchInputWidget searchInputWidget;
    private ScrollBoxWidget searchResultsWidget;

    private static final List<SearchResultData> SEARCH_DATA;

    static
    {
        SEARCH_DATA = BuiltInRegistries.ITEM.stream().map(SearchResultData::fromItem).toList();
    }

    public SpotlightScreen()
    {
        super(Component.literal("Spotlight Menu"));
    }

    @Override
    protected void init()
    {
        final int searchBarX = (this.width - SEARCH_BAR_WIDTH) / 2;
        final int searchBarY = (this.height - SEARCH_BAR_HEIGHT) / 2 - DISTANCE_FRON_CENTER;

        this.searchInputWidget = SearchInputWidget.builder(searchBarX, searchBarY, SEARCH_BAR_WIDTH, SEARCH_BAR_HEIGHT).build();
        this.searchHotbarWidget = SearchHotbarWidget.builder(
                searchBarX,
                searchBarY - HOTBAR_OFFSET,
                SEARCH_BAR_WIDTH,
                SEARCH_BAR_HEIGHT
        ).build();
        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                searchInputWidget.getY() + SEARCH_BAR_HEIGHT - searchInputWidget.getOutlineThickness(),
                searchInputWidget.getWidth(),
                RESULTS_HEIGHT
        ).showScrollerAlways(true).build();

        this.searchInputWidget.subscribeToTypeCallback(this::onType);

        this.addRenderableWidget(searchHotbarWidget);
        this.addRenderableWidget(searchInputWidget);
        this.addRenderableWidget(searchResultsWidget);

        this.setFocused(searchInputWidget);
    }

    /* Callbacks */

    private void onType(String text)
    {
        this.searchResultsWidget.removeAllChildren();

        String searchText = text.toLowerCase();

        if (searchText.isEmpty())
        {
            return;
        }

        for (SearchResultData result : SEARCH_DATA)
        {
            if (result.getName().contains(searchText) || result.getIdentifier().contains(searchText))
            {
                this.searchResultsWidget.addChildRow(SearchResultsWidget.builder(0, 0, result)
                                                                        .width(searchResultsWidget.getMaxWidth())
                                                                        .onClick(onResultClicked(result)).build());
            }
        }
    }

    private BiConsumer<MouseButtonClick, Boolean> onResultClicked(SearchResultData block)
    {
        SpotlightEntryClient.LOGGER.info("Clicked on search result: {}", block.getName());
        return (mouseButtonClick, isDoubleClick) -> {
            SpotlightEntryClient.LOGGER.info("Clicked on search result: {}", block.getName());
            // give the player the result they clicked on
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null)
                return;

            player.connection.sendCommand(block.getCommandString());
        };
    }

    /* Input */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (closeSpotlightKeyMapping.matches(keyCode, scanCode))
        {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        if (!searchInputWidget.isMouseOver(d, e) //
                && !searchResultsWidget.isMouseOver(d, e) //
                && !searchHotbarWidget.isMouseOver(d, e) //
        )
            this.onClose(); // close on any mouse click outside of search bar
        return super.mouseClicked(d, e, i);
    }

    /* `super` Settings */

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f)
    {
        // no background rendering
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}