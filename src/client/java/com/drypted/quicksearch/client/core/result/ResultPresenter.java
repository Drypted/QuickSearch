package com.drypted.quicksearch.client.core.result;

import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.commands.Command;
import com.drypted.quicksearch.client.core.blueprints.feedback.errors.SearchNotFoundError;
import com.drypted.quicksearch.client.ui.components.*;
import com.drypted.quicksearch.client.ui.state.ViewState;
import com.drypted.quicksearch.client.ui.state.VisibilityController;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class ResultPresenter
{
    private static final int HOTBAR_SLOTS = 9;

    private final InputWidget inputWidget;
    private final ScrollBoxWidget searchResultsWidget;
    private final VisibilityController visibilityController;
    private final ViewState viewState;
    private final Consumer<ItemsResultData> onItemClicked;
    private final Consumer<Command> onCommandClicked;
    private final Consumer<String> onArgSuggestionClicked;

    public ResultPresenter(InputWidget inputWidget, ScrollBoxWidget searchResultsWidget, VisibilityController visibilityController, ViewState viewState, Consumer<ItemsResultData> onItemClicked, Consumer<Command> onCommandClicked, Consumer<String> onArgSuggestionClicked)
    {
        this.inputWidget = inputWidget;
        this.searchResultsWidget = searchResultsWidget;
        this.visibilityController = visibilityController;
        this.viewState = viewState;
        this.onItemClicked = onItemClicked;
        this.onCommandClicked = onCommandClicked;
        this.onArgSuggestionClicked = onArgSuggestionClicked;
    }

    public void hideAndClearResults()
    {
        visibilityController.setItemResultsVisible(false);
        clearResults();
    }

    public void clearResults()
    {
        this.inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
        this.inputWidget.clearError();
        this.inputWidget.clearSuggestion();
        this.searchResultsWidget.removeAllChildren();
        this.viewState.setSubmitItemResult(null);
        visibilityController.clearHotbarResults();
    }

    public void displayItems(List<ItemsResultData> results)
    {
        clearResults();

        if (results.isEmpty())
        {
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            visibilityController.setItemResultsVisible(false);
            inputWidget.showError(new SearchNotFoundError());
            return;
        }

        viewState.setSubmitItemResult(results.getFirst());
        inputWidget.clearError();

        String currentText = inputWidget.getText().trim();
        if (!currentText.isEmpty())
        {
            ItemsResultData topResult = results.getFirst();
            String itemName = topResult.getName();
            if (itemName.toLowerCase().startsWith(currentText.toLowerCase()) && !itemName.equalsIgnoreCase(currentText))
            {
                inputWidget.setSuggestion(itemName);
            }
        }

        @Nullable HotbarCollectionWidget hotbarCollectionWidget = visibilityController.getHotbarCollectionWidget();
        int matchCount = 0;

        for (ItemsResultData result : results)
        {
            if (hotbarCollectionWidget != null && matchCount < HOTBAR_SLOTS)
            {
                HotbarSlotWidget widget = hotbarCollectionWidget.getWidgets().get(matchCount);
                if (widget != null)
                {
                    widget.setSearchResultData(result);
                    widget.onClick(mouseButtonClick -> {
                        onItemClicked.accept(result);
                        widget.setFocused(true);
                    });
                }
            }

            this.searchResultsWidget.addChildRow(
                    ResultDataWidget.builder(0, 0, result.getIcon(), result.getName(), result.getSerializedDefinition())
                            .width(searchResultsWidget.getChildWidth())
                            .onClick((mBC, dC) -> onItemClicked.accept(result))
                            .build()
            );

            matchCount++;
        }

        visibilityController.setItemResultsVisible(true);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    public void displayCommands(List<Command> results)
    {
        clearResults();

        if (results.isEmpty())
        {
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            visibilityController.setItemResultsVisible(false);
            return;
        }

        String currentText = inputWidget.getText().trim();
        if (!currentText.isEmpty() && !currentText.contains(" "))
        {
            Command topResult = results.getFirst();
            String commandWithSlash = "/" + topResult.getName();
            if (commandWithSlash.toLowerCase()
                    .startsWith(currentText.toLowerCase()) && !commandWithSlash.equalsIgnoreCase(currentText))
            {
                inputWidget.setSuggestion(commandWithSlash);
            }
        }

        for (Command result : results)
        {
            this.searchResultsWidget.addChildRow(
                    ResultDataWidget.builder(0, 0, null, result.getName(), result.getDescription())
                            .width(searchResultsWidget.getChildWidth())
                            .disabled(true)
                            .paddingX(10)
                            .onClick((mBC, dC) -> onCommandClicked.accept(result))
                            .build()
            );
        }

        visibilityController.setItemResultsVisible(true);
        visibilityController.setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    public void displayArgSuggestions(Command command, String[] args)
    {
        clearResults();

        this.searchResultsWidget.addChildRow(ResultDataWidget.builder(0, 0, null, command.getUsage(), null)
                                                     .width(searchResultsWidget.getChildWidth())
                                                     .disabled(true)
                                                     .build());

        List<String> suggestions = command.getSuggestions(args);

        if (suggestions.isEmpty())
        {
            visibilityController.setItemResultsVisible(true);
            visibilityController.setHotbarWidgetVisible(false);
            inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
            return;
        }

        String currentText = inputWidget.getText();
        String currentPartial = args.length > 0 ? args[args.length - 1] : "";
        String topSuggestion = suggestions.getFirst();
        if (topSuggestion.toLowerCase().startsWith(currentPartial.toLowerCase())
                && !topSuggestion.equalsIgnoreCase(currentPartial))
        {
            String prefix = currentText.substring(0, currentText.length() - currentPartial.length());
            inputWidget.setSuggestion(prefix + topSuggestion);
        }

        for (final String suggestion : suggestions)
        {
            this.searchResultsWidget.addChildRow(ResultDataWidget.builder(0, 0, null, null, suggestion)
                                                         .width(searchResultsWidget.getChildWidth())
                                                         .paddingX(7)
                                                         .paddingY(4)
                                                         .onClick((mBC, dC) -> onArgSuggestionClicked.accept(suggestion))
                                                         .build());
        }

        visibilityController.setItemResultsVisible(true);
        visibilityController.setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }
}
