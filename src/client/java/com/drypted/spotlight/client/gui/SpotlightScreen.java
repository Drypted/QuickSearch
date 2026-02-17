package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.actions.Actions;
import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.handlers.CommandsHandler;
import com.drypted.spotlight.client.core.handlers.SearchHandler;
import com.drypted.spotlight.client.gui.components.*;
import com.drypted.spotlight.client.models.ItemsResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
    private @Nullable HotbarCollectionWidget hotbarCollectionWidget;

    @Nullable
    private Command selectedCommand = null;

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

        this.inputWidget = InputWidget.builder(searchBarX, searchBarY, searchBarWidth, SEARCH_BAR_HEIGHT).build();
        this.inputWidget.setPlaceholder("Search items for blocks ...");
        // set validator for no symbols
        this.inputWidget.setValidator(text -> text.matches("[/a-zA-Z0-9 _-]*"));

        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                inputWidget.getY() + SEARCH_BAR_HEIGHT - inputWidget.getOutlineThickness(),
                inputWidget.getWidth(),
                resultsHeight
        ).showScrollerAlways(true).build();

        this.inputWidget.addTextChangeListener(this::onTextChanged);

        if (isHotbarEnabledInConfig())
        {
            this.hotbarCollectionWidget = HotbarCollectionWidget.create(
                    searchBarX,
                    searchBarWidth,
                    searchBarY - HotbarCollectionWidget.HOTBAR_SLOT_PADDING
            );
            this.addRenderableWidget(hotbarCollectionWidget);
        }

        this.addRenderableWidget(searchResultsWidget);
        this.addRenderableWidget(inputWidget);

        // show search on open
        setItemResultsVisible(false);

        this.setFocused(inputWidget);
    }

    /* Input */

    private void onTextChanged(String text)
    {
        if (text == null || text.isEmpty())
        {
            setItemResultsVisible(false);
            clearResults();
            return;
        }

        // Set visual state to searching
        inputWidget.setSearchStatus(InputWidget.SearchStatus.SEARCHING);

        // Delegate logic to appropriate handler
        if (text.contains("/")) CommandsHandler.getCommands(text, this::displayCommands);
        else SearchHandler.searchAsync(text, this::displayItems);
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
                setItemResultsVisible(false);
                return true;
            }
            this.onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            SpotlightEntryClient.LOGGER.info("Enter pressed, executing commands if valid");
            onEnterPressed();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            SpotlightEntryClient.LOGGER.info("Tab pressed, executing tab action if valid");
            onTabPressed();
            return true;
        }

        if (this.minecraft == null) return super.keyPressed(keyCode, scanCode, modifiers);

        // only allow key when hotbar is focused
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null && hotbarCollectionWidget.isFocused())
        {
            for (int i = 0; i < HOTBAR_SLOTS; i++)
            {
                HotbarSlotWidget hotbarWidget = this.hotbarCollectionWidget.getWidgets().get(i);
                if (hotbarWidget != null && keyCode == this.minecraft.options.keyHotbarSlots[i].getDefaultKey().getValue())
                {
                    this.hotbarCollectionWidget.onHotbarKeyPressed(hotbarWidget, modifiers);
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null)
            this.hotbarCollectionWidget.onAnyKeyReleased();

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

    private void displayItems(List<ItemsResultData> results)
    {
        clearResults();

        // If results came back empty (or query was canceled/cleared mid-flight), stop here.
        if (results.isEmpty())
        {
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            setItemResultsVisible(false);
            return;
        }

        int matchCount = 0;
        for (ItemsResultData result : results)
        {
            // fill hotbar for first 9
            if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null && matchCount < HOTBAR_SLOTS)
            {
                HotbarSlotWidget widget = this.hotbarCollectionWidget.getWidgets().get(matchCount);
                if (widget != null)
                {
                    widget.setSearchResultData(result);
                    widget.onClick(mouseButtonClick -> {
                        onItemsResultClicked(result);
                        widget.setFocused(true);
                    });
                }
            }

            this.searchResultsWidget.addChildRow( //
                    ItemsResultDataWidget.builder(
                            0,
                            0,
                            result
                    ).width(searchResultsWidget.getMaxWidth()).onClick((mBC, dC) -> onItemsResultClicked(result)).build() //
            );

            matchCount++;
        }

        setItemResultsVisible(true);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    private void onItemsResultClicked(ItemsResultData data)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) Actions.giveItem(player, data);
    }

    /* Commands */

    private void displayCommands(List<Command> results)
    {
        clearResults();

        // If results came back empty (or query was canceled/cleared mid-flight), stop here.
        if (results.isEmpty())
        {
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            setItemResultsVisible(false);
            selectedCommand = null;
            return;
        }

        selectedCommand = results.getFirst(); // auto-select first commands for hotbar keybinds

        for (Command result : results)
        {
            this.searchResultsWidget.addChildRow(CommandResultDataWidget.builder(
                            0,
                            0,
                            result
                    ).width(searchResultsWidget.getMaxWidth()).onClick((mBC, dC) -> onCommandsResultMouseClick(result)).build() //
            );
        }

        setItemResultsVisible(true);
        setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    private void onCommandsResultMouseClick(Command data)
    {
        if (!data.isNotArgs()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) data.execute(new String[]{}, player);
    }

    private void onEnterPressed()
    {
        String text = inputWidget.getText();
        if (text == null || !isUserInputCommand()) return;

        SpotlightEntryClient.LOGGER.info("Executing commands from user input: {}", text);


        String[] parts = text.split(" ");
        String commandName = parts[0].substring(1); // Remove leading "/"
        String[] args = new String[0];
        if (parts.length > 1)
        {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        }

        SpotlightEntryClient.LOGGER.info("Detected commands: {}, with args: {}", commandName, String.join(", ", args));


        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (selectedCommand != null && selectedCommand.getName().equalsIgnoreCase(commandName))
        {
            selectedCommand.execute(args, player);
        }

        // execute and exit
        this.onClose();
    }

    private void onTabPressed()
    {
        if (isUserInputCommand() && selectedCommand != null)
        {
            String commandName = selectedCommand.getName();
            inputWidget.setText("/" + commandName + " ");
        }
    }

    /* Overrides for settings */

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f)
    {
    } // don't render any background

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    /* Helpers */

    public void setVisible(AbstractWidget widget, boolean visible)
    {
        if (widget == null) return;

        widget.visible = visible;
        widget.active = visible;
    }

    private void setItemResultsVisible(boolean visible)
    {
        setHotbarWidgetVisible(visible);
        setVisible(this.searchResultsWidget, visible);
    }

    private void setHotbarWidgetVisible(boolean visible)
    {
        if (!isHotbarEnabledInConfig() || this.hotbarCollectionWidget == null) return;

        this.hotbarCollectionWidget.getWidgets().forEach(widget -> setVisible(widget, visible));
        setVisible(this.hotbarCollectionWidget, visible);
    }

    private void clearResults()
    {
        this.inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
        this.searchResultsWidget.removeAllChildren();
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null)
            this.hotbarCollectionWidget.getWidgets().forEach(widget -> widget.setSearchResultData(null));
    }

    private boolean isHotbarEnabledInConfig()
    {
        return SpotlightEntryClient.getConfig().showHotbarSlots;
    }

    private boolean isUserInputCommand()
    {
        String text = inputWidget.getText();
        return text != null && text.contains("/");
    }
}