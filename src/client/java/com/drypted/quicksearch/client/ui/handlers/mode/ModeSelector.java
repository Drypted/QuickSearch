package com.drypted.quicksearch.client.ui.handlers.mode;

import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.ui.state.ResultPresenter;
import com.drypted.quicksearch.client.ui.components.InputWidget;

/// <summary>
/// Determines whether the user is inputting a command or an item query and routes the mode to the appropriate handler.
/// </summary>
public final class ModeSelector
{
    private final InputWidget inputWidget;
    private final ResultPresenter resultPresenter;
    private final CommandsModeHandler commandsModeHandler;
    private final ItemModeHandler itemModeHandler;

    public ModeSelector(InputWidget inputWidget, ResultPresenter resultPresenter, CommandsModeHandler commandsModeHandler, ItemModeHandler itemModeHandler)
    {
        this.inputWidget = inputWidget;
        this.resultPresenter = resultPresenter;
        this.commandsModeHandler = commandsModeHandler;
        this.itemModeHandler = itemModeHandler;
    }

    public void onTextChanged(String text)
    {
        if (text == null || text.isEmpty())
        {
            resultPresenter.hideAndClearResults();
            return;
        }

        inputWidget.setSearchStatus(InputWidget.SearchStatus.SEARCHING);

        if (CommandInputParser.isCommandInput(text))
        {
            commandsModeHandler.handle(text);
            return;
        }

        itemModeHandler.handle(text);
    }
}
