package com.drypted.spotlight.client.ui;

import com.drypted.spotlight.client.SpotlightClient;
import com.drypted.spotlight.client.core.actions.GiveItemAction;
import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import com.drypted.spotlight.client.core.blueprints.commands.Command;
import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.feedback.errors.InvalidItemError;
import com.drypted.spotlight.client.core.blueprints.feedback.errors.SearchNotFoundError;
import com.drypted.spotlight.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.spotlight.client.core.handlers.CommandsHandler;
import com.drypted.spotlight.client.core.handlers.SearchHandler;
import com.drypted.spotlight.client.init.ModKeybinds;
import com.drypted.spotlight.client.ui.components.*;
import com.drypted.spotlight.client.ui.renderer.MosaicBackgroundRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotlightScreen extends Screen
{
    private static final int SEARCH_BAR_HEIGHT = 22;
    private static final int DISTANCE_FROM_CENTER = 20;
    private static final int HOTBAR_SLOTS = 9;

    private InputWidget inputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private @Nullable HotbarCollectionWidget hotbarCollectionWidget;
    /// What item to give user on input submit
    private @Nullable ItemsResultData submitItemResult;

    private final boolean showCommandOnStartup;

    /// Keep track of the last query
    private static String lastQuery = "";

    public SpotlightScreen(boolean showCommandOnStartup)
    {
        super(Component.literal("Spotlight Menu"));
        this.showCommandOnStartup = showCommandOnStartup;
    }

    @Override
    public void onClose()
    {
        lastQuery = this.inputWidget.getText();
        super.onClose();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        MosaicBackgroundRenderer.captureFramebuffer();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void init()
    {
        final int searchBarWidth = SpotlightClient.getConfig().ui.searchBarWidth;
        final int resultsHeight = SpotlightClient.getConfig().ui.resultsBoxHeight;

        final int searchBarX = (this.width - searchBarWidth) / 2;
        final int searchBarY = (this.height - SEARCH_BAR_HEIGHT) / 2 - DISTANCE_FROM_CENTER;

        this.inputWidget = InputWidget.builder(searchBarX, searchBarY, searchBarWidth, SEARCH_BAR_HEIGHT).build();
        this.inputWidget.setPlaceholder("Search items for blocks ...");
        // set validator for no symbols
        this.inputWidget.setValidator(text -> text.matches("[/a-zA-Z0-9 -_\"]*"));

        this.searchResultsWidget = ScrollBoxWidget.builder(
                searchBarX,
                (int) (inputWidget.getY() + SEARCH_BAR_HEIGHT - inputWidget.getOutlineThickness()),
                inputWidget.getWidth(),
                resultsHeight
        ).showScrollerAlways(true).build();

        this.inputWidget.addTextChangeListener(this::onTextChanged);
        this.inputWidget.addSubmitListener((text) -> {
            if (isUserInputCommand())
            {
                onSubmitCommand(text);
            }
            else
            {
                onSubmitItem();
            }
        });

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

        boolean isLastQueryCommand = lastQuery.startsWith("/");

        if (showCommandOnStartup)
        {
            if (isLastQueryCommand) inputWidget.setSuggestion(lastQuery);
            else inputWidget.setText("/"); // reset
        }
        else
        {
            if (isLastQueryCommand) inputWidget.setText(""); // reset
            else inputWidget.setSuggestion(lastQuery);
        }

        // ignore last query if config is set to not remember it
        if (!SpotlightClient.getConfig().search.rememberLastQuery) lastQuery = "";
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
        if (text.startsWith("/"))
        {
            String afterSlash = text.substring(1);
            String commandName = afterSlash.split("\\s+")[0];
            boolean hasSpaceAfterCommand = afterSlash.length() > commandName.length();

            Command command = CommandsHandler.getRawCommand(commandName);

            if (command != null && hasSpaceAfterCommand)
            {
                // User has typed a valid command name + space → show argument suggestions
                String[] args = getArgs();
                displayArgSuggestions(command, args);
                inputWidget.showError(command.validateArgs(args));
            }
            else
            {
                // Still typing the command name → show command list
                CommandsHandler.getCommands(text, this::displayCommands);

                if (command != null)
                {
                    inputWidget.showError(command.validateArgs(getArgs()));
                }
            }
        }
        else
        {
            SearchHandler.searchAsync(text, this::displayItems);
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent kEv)
    {
        if (ModKeybinds.getCloseSpotlightKey().matches(kEv))
        {
            // remove suggestion on close if config is set to not remember last query
            if (inputWidget.hasSuggestion() && !SpotlightClient.getConfig().search.rememberLastQuery)
            {
                inputWidget.clearSuggestion();
                return true;
            }

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

        // only allow key when hotbar is focused
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null && hotbarCollectionWidget.isFocused())
        {
            for (int i = 0; i < HOTBAR_SLOTS; i++)
            {
                HotbarSlotWidget hotbarWidget = this.hotbarCollectionWidget.getWidgets().get(i);
                if (hotbarWidget != null && kEv.key() == this.minecraft.options.keyHotbarSlots[i].getDefaultKey()
                        .getValue())
                {
                    this.hotbarCollectionWidget.onHotbarKeyPressed(hotbarWidget, kEv.modifiers());
                    return true;
                }
            }
        }

        return super.keyPressed(kEv);
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent kEv)
    {
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null)
            this.hotbarCollectionWidget.onAnyKeyReleased();

        return super.keyReleased(kEv);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mEv, boolean doubleClick)
    {
        if (!super.mouseClicked(mEv, doubleClick))
        {
            // click outside, close spotlight
            this.onClose();
            return true;
        }
        return super.mouseClicked(mEv, doubleClick);
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
            inputWidget.showError(new SearchNotFoundError());
            return;
        }

        submitItemResult = results.getFirst();

        inputWidget.clearError();

        // Set suggestion to first result if user is still typing the command name
        String currentText = inputWidget.getText().trim();
        if (!currentText.isEmpty() && !results.isEmpty())
        {
            ItemsResultData topResult = results.getFirst();
            String itemName = topResult.getName();
            if (itemName.toLowerCase().startsWith(currentText.toLowerCase()) && !itemName.equalsIgnoreCase(currentText))
            {
                inputWidget.setSuggestion(itemName);
            }
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
                    ResultDataWidget.builder(0, 0, result.getIcon(), result.getName(), result.getSerializedDefinition())
                            .width(searchResultsWidget.getChildWidth())
                            .onClick((mBC, dC) -> onItemsResultClicked(result))
                            .build() //
            );

            matchCount++;
        }

        setItemResultsVisible(true);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    private void onItemsResultClicked(ItemsResultData data)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) GiveItemAction.run(player, data);
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
            return;
        }

        // Set suggestion to first result if user is still typing the command name
        String currentText = inputWidget.getText().trim();
        if (!currentText.isEmpty() && !currentText.contains(" ") && !results.isEmpty())
        {
            Command topResult = results.getFirst();
            String commandWithSlash = "/" + topResult.getName();
            if (commandWithSlash.toLowerCase().startsWith(currentText.toLowerCase()) && !commandWithSlash.equalsIgnoreCase(currentText))
            {
                inputWidget.setSuggestion(commandWithSlash);
            }
        }

        for (Command result : results)
        {
            this.searchResultsWidget.addChildRow( //
                    ResultDataWidget.builder(0, 0, null, result.getName(), result.getDescription())
                            .width(searchResultsWidget.getChildWidth())
                            .disabled(true)
                            .paddingX(10)
                            .onClick((mBC, dC) -> onCommandsResultMouseClick(result))
                            .build() //
            );
        }

        setItemResultsVisible(true);
        setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    /**
     * Display argument suggestions for a recognized command. Shows suggestions in the dropdown and sets the top
     * suggestion as ghost text.
     */
    private void displayArgSuggestions(Command command, String[] args)
    {
        clearResults();

        // always show usage hint at top
        this.searchResultsWidget.addChildRow(ResultDataWidget.builder(0, 0, null, command.getUsage(), null)
                .width(searchResultsWidget.getChildWidth())
                .disabled(true)
                .build());

        List<String> suggestions = command.getSuggestions(args);

        if (suggestions.isEmpty())
        {
            setItemResultsVisible(true);
            setHotbarWidgetVisible(false);
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            return;
        }

        // Set ghost-text suggestion: build the full input text with the top suggestion completing the current arg
        String currentText = inputWidget.getText();
        String currentPartial = args.length > 0 ? args[args.length - 1] : "";
        if (!suggestions.isEmpty())
        {
            String topSuggestion = suggestions.getFirst();
            if (topSuggestion.toLowerCase().startsWith(currentPartial.toLowerCase()) && !topSuggestion.equalsIgnoreCase(currentPartial))
            {
                // Build the full suggestion: current text up to the partial, then the full suggestion
                String prefix = currentText.substring(0, currentText.length() - currentPartial.length());
                inputWidget.setSuggestion(prefix + topSuggestion);
            }
        }

        // Populate the dropdown with all suggestions
        for (final String suggestion : suggestions)
        {
            this.searchResultsWidget.addChildRow(ResultDataWidget.builder(0, 0, null, null, suggestion)
                    .width(searchResultsWidget.getChildWidth())
                    .paddingX(7)
                    .paddingY(4)
                    .onClick((mBC, dC) -> onArgSuggestionClicked(suggestion))
                    .build());
        }

        setItemResultsVisible(true);
        setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    /**
     * Called when user clicks an argument suggestion in the dropdown. Replaces the current partial argument with the
     * selected suggestion.
     */
    private void onArgSuggestionClicked(String suggestion)
    {
        String currentText = inputWidget.getText();
        String[] args = getArgs();
        String currentPartial = args.length > 0 ? args[args.length - 1] : "";

        // Replace the current partial with the full suggestion
        String newText;
        if (!currentPartial.isEmpty())
        {
            newText = currentText.substring(0, currentText.length() - currentPartial.length()) + suggestion;
        }
        else
        {
            // No partial yet — append suggestion after the trailing space
            newText = currentText + suggestion;
        }

        inputWidget.setText(newText);
    }

    private void onCommandsResultMouseClick(Command data)
    {
        if (data.validateArgs(getArgs()).haltsExecution()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) data.execute(new String[]{}, player);
    }

    private void onSubmitCommand(String text)
    {
        String commandName = text.split(" ")[0].substring(1); // Remove leading "/"

        String[] args = getArgs();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        CommandFeedback error = CommandsHandler.execute(commandName, args, player);
        if (error.haltsExecution())
        {
            this.inputWidget.showError(error);
            return;
        }

        if (!error.getMessage().isEmpty()) //
            player.displayClientMessage(
                    Component.literal(error.getSeverity().getName() + ": " + error.getMessage()) //
                            .withStyle(error.getSeverity().getChatColor()), false
            );

        this.onClose();
    }

    private void onSubmitItem()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean invalidItem = false;

        if (this.submitItemResult != null)
        {
            GiveItemAction.run(player, this.submitItemResult);
        }
        else
        {
            invalidItem = true;
        }

        if (invalidItem)
        {
            this.inputWidget.showError(new InvalidItemError());
            return;
        }

        this.onClose();
    }

    private String @NotNull [] getArgs()
    {
        String text = this.inputWidget.getText().trim();
        int spaceIndex = text.indexOf(' ');

        // no space -> no arguments
        if (spaceIndex == -1) return new String[0];

        // separate command name from args
        String args = text.substring(spaceIndex + 1).trim();
        if (args.isEmpty()) return new String[0];

        List<String> matches = new ArrayList<>();

        // text inside double quotes: "([^"]*)"
        // words separated by spaces: ([^\s"]+)
        Pattern regex = Pattern.compile("\"([^\"]*)\"|([^\\s\"]+)");
        Matcher matcher = regex.matcher(args);

        while (matcher.find())
        {
            if (matcher.group(1) != null)
            {
                // Found a quoted string, add it without the quotes
                matches.add(matcher.group(1));
            }
            else if (matcher.group(2) != null)
            {
                // Found a normal word
                matches.add(matcher.group(2));
            }
        }

        return matches.toArray(new String[0]);
    }

    /* Overrides for settings */

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int i, int j, float f)
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

        if (visible) this.inputWidget.setRounded(RoundedCorners.fromVerticalSides(true, false));
        else this.inputWidget.setRounded(RoundedCorners.all());
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
        this.inputWidget.clearError();
        this.inputWidget.clearSuggestion();
        this.searchResultsWidget.removeAllChildren();
        this.submitItemResult = null;
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null)
            this.hotbarCollectionWidget.getWidgets().forEach(widget -> widget.setSearchResultData(null));
    }

    private boolean isHotbarEnabledInConfig()
    {
        return SpotlightClient.getConfig().hotbar.showHotbarSlots;
    }

    private boolean isUserInputCommand()
    {
        String text = inputWidget.getText();
        return text != null && text.startsWith("/");
    }
}