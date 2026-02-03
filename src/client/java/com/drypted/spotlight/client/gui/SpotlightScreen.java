package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.SearchHandler;
import com.drypted.spotlight.client.core.models.SearchResultData;
import com.drypted.spotlight.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SpotlightScreen extends Screen
{
    private static final int SEARCH_BAR_WIDTH = 200;
    private static final int SEARCH_BAR_HEIGHT = 22;

    private static final int HOTBAR_SLOT_PADDING = 2;

    private static final int DISTANCE_FROM_CENTER = 20;
    private static final int RESULTS_HEIGHT = 100;
    private static final int HOTBAR_SLOTS = 9;

    private SearchInputWidget searchInputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private final ArrayList<SearchHotbarWidget> searchHotbarWidgets = new ArrayList<>(HOTBAR_SLOTS);
    private HotbarFocusProxyWidget hotbarFocusProxy;

    private SearchHotbarWidget selectedHotbarWidget = null;

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
        this.searchInputWidget.setPlaceholder("Search items for blocks ...");
        // set validator for no symbols
        this.searchInputWidget.setValidator(text -> text.matches("[a-zA-Z0-9 _-]*"));

        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                searchInputWidget.getY() + SEARCH_BAR_HEIGHT - searchInputWidget.getOutlineThickness(),
                searchInputWidget.getWidth(),
                RESULTS_HEIGHT
        ).showScrollerAlways(true).build();

        generateHotbarWidgets(searchBarY, searchBarX);

        this.searchInputWidget.addTypeListener(this::onType);

        this.searchHotbarWidgets.forEach(this::addRenderableOnly);

        this.addWidget(hotbarFocusProxy);
        this.addRenderableWidget(searchResultsWidget);
        this.addRenderableWidget(searchInputWidget);

        // show search on open
        // setVisible(this.searchResultsWidget, false);
        // setVisible(this.hotbarFocusProxy, false);
        this.searchHotbarWidgets.forEach(widget -> setVisible(widget, false));

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
            final SearchHotbarWidget hotbarWidget = SearchHotbarWidget.builder(
                    i, //
                    (int) Math.ceil(cursor),
                    startY,
                    (int) Math.ceil(iconSize),
                    (int) Math.ceil(iconSize)
            ).build();
            hotbarWidget.setOnClickCallback(mouseButtonClick -> {
                SearchResultData item = hotbarWidget.getSearchResultData();
                if (item == null || item.isEmpty())
                    return;

                onHotbarKeyPressed(hotbarWidget, 0);
            });
            this.searchHotbarWidgets.add(hotbarWidget);
            cursor += iconSize + HOTBAR_SLOT_PADDING;
        }

        // generate proxy for hotbar focus navigation
        this.hotbarFocusProxy = HotbarFocusProxyWidget.create(searchHotbarWidgets);
        this.hotbarFocusProxy.setOnFocusChanged((focused) -> {
            if (!focused)
                this.selectedHotbarWidget = null;
        });
    }

    private void displayResults(List<SearchResultData> results)
    {
        clearResults();

        // If results came back empty (or query was canceled/cleared mid-flight), stop here.
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
                    SearchResultsWidgetEntry.builder(0, 0, result)
                                            .width(searchResultsWidget.getMaxWidth())
                                            .onClick((mBC, dC) -> onResultClicked(result))
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
        searchHotbarWidgets.forEach(widget -> widget.setSearchResultData(null));
    }

    private void onResultClicked(SearchResultData block)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
            player.connection.sendCommand(block.getCommandString());
    }

    /* Input */

    private void onType(String text, char typedChar)
    {
        if (text == null || text.isEmpty())
        {
            this.searchHotbarWidgets.forEach(widget -> setVisible(widget, false));
            setVisible(this.searchResultsWidget, false);
            setVisible(this.hotbarFocusProxy, false);
            clearResults();
            return;
        }

        // Set visual state to searching
        searchInputWidget.setSearchStatus(SearchInputWidget.SearchStatus.SEARCHING);
        this.searchHotbarWidgets.forEach(widget -> setVisible(widget, true));
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
            if (searchInputWidget.isFocused() && searchInputWidget.hasText())
            {
                searchInputWidget.clearText();
                clearResults();
                this.searchHotbarWidgets.forEach(widget -> setVisible(widget, false));
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
                SearchHotbarWidget hotbarWidget = searchHotbarWidgets.get(i);
                if (hotbarWidget != null && keyCode == this.minecraft.options.keyHotbarSlots[i].getDefaultKey()
                                                                                               .getValue())
                {
                    onHotbarKeyPressed(hotbarWidget, modifiers);
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onHotbarKeyPressed(SearchHotbarWidget widget, int modifiers)
    {
        // if shift pressed, select hotbar slot only
        if (isModifierPressed(modifiers, GLFW.GLFW_MOD_SHIFT))
        {
            this.selectedHotbarWidget = widget;
            this.hotbarFocusProxy.highlightSlot(selectedHotbarWidget.getHotbarIndex());
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
        {
            // if there is item in the selected hotbar slot
            SearchResultData item = widget.getSearchResultData();

            String identifier = "minecraft:air";
            int count = 1;
            // if a slot is already selected, use that slot
            if (selectedHotbarWidget != null)
            {
                if (selectedHotbarWidget.getSearchResultData() != null)
                {
                    identifier = selectedHotbarWidget.getSearchResultData()
                                                     .getIdentifier()
                                                     .toString();
                    count = selectedHotbarWidget.getSearchResultData().getIcon().getMaxStackSize();
                }

                // used this one
                selectedHotbarWidget = null;
                this.hotbarFocusProxy.unhighlightAllSlots();
            }
            else if (item != null)
            {
                identifier = item.getIdentifier().toString();
                count = item.getMaxStackSize();
            }

            // replace item in hotbar slot
            String command = String.format(
                    "item replace entity @s hotbar.%d with %s %d",
                    widget.getHotbarIndex(),
                    identifier,
                    count
            );

            player.connection.sendCommand(command);
        }
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

    private static boolean isModifierPressed(int modifiers, int modifierToCheck)
    {
        return (modifiers & modifierToCheck) != 0;
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
        System.out.println("Changed focus to " + componentPath.toString());
    }
}