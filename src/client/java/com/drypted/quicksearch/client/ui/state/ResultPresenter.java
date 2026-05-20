package com.drypted.quicksearch.client.ui.state;

import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.commands.Command;
import com.drypted.quicksearch.client.core.blueprints.feedback.errors.SearchNotFoundError;
import com.drypted.quicksearch.client.ui.components.*;
import com.drypted.quicksearch.client.ui.handlers.ClickHandlers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ResultPresenter
{
    private static final int HOTBAR_SLOTS = 9;

    private final InputWidget inputWidget;
    private final ScrollBoxWidget searchResultsWidget;
    private final VisibilityController visibilityController;
    private final ClickHandlers clickHandlers;
    private @Nullable ItemsResultData submitItemResult;

    public ResultPresenter(InputWidget inputWidget, ScrollBoxWidget searchResultsWidget, VisibilityController visibilityController)
    {
        this.inputWidget = inputWidget;
        this.searchResultsWidget = searchResultsWidget;
        this.visibilityController = visibilityController;
        this.clickHandlers = new ClickHandlers(inputWidget);
    }

    public @Nullable ItemsResultData getSubmitItemResult()
    {
        return submitItemResult;
    }

    private void setSubmitItemResult(@Nullable ItemsResultData submitItemResult)
    {
        this.submitItemResult = submitItemResult;
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
        this.setSubmitItemResult(null);
        this.visibilityController.clearHotbarResults();
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

        setSubmitItemResult(results.getFirst());
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
                        clickHandlers.onItemClicked(result);
                        widget.setFocused(true);
                    });
                }
            }

            ResultDataWidget widget = new ResultDataWidget(
                            0,
                            0,
                            result.getIcon(),
                            result.getName(),
                            result.getSerializedDefinition()
                    );
            widget.setWidth(searchResultsWidget.getChildWidth());
            widget.onClick((mBC, dC) -> clickHandlers.onItemClicked(result));

            this.searchResultsWidget.addChildRow(widget);

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
            ResultDataWidget widget = new ResultDataWidget(0, 0, null, result.getName(), result.getDescription());
            widget.setWidth(searchResultsWidget.getChildWidth());
            widget.setDisabled(true);
            widget.setPaddingX(10);
            widget.onClick((mBC, dC) -> clickHandlers.onCommandClicked(result));

            this.searchResultsWidget.addChildRow(widget);
        }

        visibilityController.setItemResultsVisible(true);
        visibilityController.setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }

    public void displayArgSuggestions(Command command, String[] args)
    {
        clearResults();

        ResultDataWidget usageWidget = new ResultDataWidget(0, 0, null, command.getUsage(), null);
        usageWidget.setWidth(searchResultsWidget.getChildWidth());
        usageWidget.setDisabled(true);

        this.searchResultsWidget.addChildRow(usageWidget);

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
        if (topSuggestion.toLowerCase().startsWith(currentPartial.toLowerCase()) && !topSuggestion.equalsIgnoreCase(
                currentPartial))
        {
            String prefix = currentText.substring(0, currentText.length() - currentPartial.length());
            inputWidget.setSuggestion(prefix + topSuggestion);
        }

        for (final String suggestion : suggestions)
        {
            ResultDataWidget suggestionWidget = new ResultDataWidget(0, 0, null, null, suggestion);
            suggestionWidget.setWidth(searchResultsWidget.getChildWidth());
            suggestionWidget.setPaddingX(7);
            suggestionWidget.setPaddingY(4);
            suggestionWidget.onClick((mBC, dC) -> clickHandlers.onCommandSuggestionClick(suggestion));

            this.searchResultsWidget.addChildRow(suggestionWidget);
        }

        visibilityController.setItemResultsVisible(true);
        visibilityController.setHotbarWidgetVisible(false);
        inputWidget.setSearchStatus(InputWidget.SearchStatus.IDLE);
    }
}
