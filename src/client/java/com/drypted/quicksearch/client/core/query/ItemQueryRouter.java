package com.drypted.quicksearch.client.core.query;

import com.drypted.quicksearch.client.core.handlers.SearchHandler;
import com.drypted.quicksearch.client.core.result.ResultPresenter;

public final class ItemQueryRouter
{
    private final ResultPresenter resultPresenter;

    public ItemQueryRouter(ResultPresenter resultPresenter)
    {
        this.resultPresenter = resultPresenter;
    }

    public void route(String text)
    {
        SearchHandler.searchAsync(text, resultPresenter::displayItems);
    }
}
