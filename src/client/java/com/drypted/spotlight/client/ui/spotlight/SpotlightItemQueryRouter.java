package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.handlers.SearchHandler;

public final class SpotlightItemQueryRouter
{
    private final SpotlightResultPresenter resultPresenter;

    public SpotlightItemQueryRouter(SpotlightResultPresenter resultPresenter)
    {
        this.resultPresenter = resultPresenter;
    }

    public void route(String text)
    {
        SearchHandler.searchAsync(text, resultPresenter::displayItems);
    }
}
