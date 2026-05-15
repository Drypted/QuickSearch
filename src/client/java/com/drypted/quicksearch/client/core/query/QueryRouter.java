package com.drypted.quicksearch.client.core.query;

import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.core.result.ResultPresenter;
import com.drypted.quicksearch.client.ui.components.InputWidget;

public final class QueryRouter
{
    private final InputWidget inputWidget;
    private final ResultPresenter resultPresenter;
    private final CommandQueryRouter commandQueryRouter;
    private final ItemQueryRouter itemQueryRouter;

    public QueryRouter(InputWidget inputWidget, ResultPresenter resultPresenter, CommandQueryRouter commandQueryRouter, ItemQueryRouter itemQueryRouter)
    {
        this.inputWidget = inputWidget;
        this.resultPresenter = resultPresenter;
        this.commandQueryRouter = commandQueryRouter;
        this.itemQueryRouter = itemQueryRouter;
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
            commandQueryRouter.route(text);
            return;
        }

        itemQueryRouter.route(text);
    }
}
