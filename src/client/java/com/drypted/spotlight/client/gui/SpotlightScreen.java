package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

import static com.drypted.spotlight.client.SpotlightEntryClient.closeSpotlightKeyMapping;

public class SpotlightScreen extends Screen
{
    private static final int SEARCH_BAR_WIDTH = 200;
    private static final int SEARCH_BAR_HEIGHT = 20;

    private static final int HOTBAR_SLOT_PADDING = 4;

    private static final int DISTANCE_FRON_CENTER = 20;
    private static final int RESULTS_HEIGHT = 100;

    private SearchInputWidget searchInputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private final HashMap<Integer, SearchHotbarWidget> searchHotbarWidgets = new HashMap<>();

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
        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                searchInputWidget.getY() + SEARCH_BAR_HEIGHT - searchInputWidget.getOutlineThickness(),
                searchInputWidget.getWidth(),
                RESULTS_HEIGHT
        ).showScrollerAlways(true).build();

        generateHotbarWidgets(searchBarY, searchBarX);

        this.searchInputWidget.subscribeToTypeCallback(this::onType);

        searchHotbarWidgets.forEach((id, widget) -> this.addRenderableWidget(widget));
        this.addRenderableWidget(searchInputWidget);
        this.addRenderableWidget(searchResultsWidget);

        this.setFocused(searchInputWidget);
        this.searchResultsWidget.visible = false;
    }

    private void generateHotbarWidgets(int searchBarY, int searchBarX)
    {
        final int slots = 9;

        // width = slots * iconSize + (slots + 1) * padding
        // => iconSize = (width - (slots + 1) * padding) / slots
        final float iconSize = (SEARCH_BAR_WIDTH - HOTBAR_SLOT_PADDING * (slots + 1)) / (float) slots;

        final int endY = searchBarY - searchInputWidget.getOutlineThickness();
        final int startY = (int) Math.ceil(endY - iconSize);

        float cursor = searchBarX + HOTBAR_SLOT_PADDING;
        for (int i = 0; i < slots; i++)
        {
            this.searchHotbarWidgets.put(
                    i,
                    SearchHotbarWidget.builder(
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
        this.searchResultsWidget.removeAllChildren();
        searchHotbarWidgets.values().forEach(widget -> widget.setSearchResultData(null));

        String searchText = text.toLowerCase();

        if (searchText.isEmpty())
        {
            return;
        }

        int matchCount = 0;
        for (SearchResultData result : SEARCH_DATA)
        {
            // no match
            if (!result.getName().contains(searchText) && !result.getIdentifier().contains(searchText))
                continue;

            // show in hotbar
            if (matchCount < 9)
            {
                SearchHotbarWidget widget = searchHotbarWidgets.get(matchCount);
                if (widget != null)
                    widget.setSearchResultData(result);
                matchCount++;
                continue;
            }

            matchCount++;

            // show in results
            this.searchResultsWidget.addChildRow(SearchResultsWidget.builder(0, 0, result)
                                                                    .width(searchResultsWidget.getMaxWidth())
                                                                    .onClick(onResultClicked(result)).build());
        }

        this.searchResultsWidget.visible = matchCount > 9;
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
            if (searchInputWidget.hasText())
            {
                searchInputWidget.clearText();
                return true;
            }
            this.onClose();
            return true;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return super.keyPressed(keyCode, scanCode, modifiers);

        // hotbar keybinds
        for (int i = 0; i < 9; i++)
        {
            if (keyCode == Minecraft.getInstance().options.keyHotbarSlots[i].getDefaultKey().getValue())
            {
                SearchHotbarWidget hotbarWidget = searchHotbarWidgets.get(i);
                if (hotbarWidget != null && hotbarWidget.getSearchResultData() != null)
                {
                    SearchResultData result = hotbarWidget.getSearchResultData();
                    player.connection.sendCommand(result.getCommandString());
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        if (!searchInputWidget.isMouseOver(d, e) && !searchResultsWidget.isMouseOver(d, e) //
                && searchHotbarWidgets.values().stream().noneMatch(widget -> widget.isMouseOver(d, e)))
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