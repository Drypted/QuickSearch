package com.drypted.quicksearch.client.ui.handlers.mode;

import com.drypted.quicksearch.client.core.handlers.SearchHandler;
import com.drypted.quicksearch.client.ui.state.ResultPresenter;

public final class ItemModeHandler
{
    private final ResultPresenter resultPresenter;

    public ItemModeHandler(ResultPresenter resultPresenter) { this.resultPresenter = resultPresenter; }

    public void handle(String text)
    {
        SearchHandler.searchAsync(text, resultPresenter::displayItems);
    }
}
