package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.input.CommandInputParser;
import com.drypted.spotlight.client.ui.components.InputWidget;

public final class SpotlightQueryRouter
{
    private final InputWidget inputWidget;
    private final SpotlightResultPresenter resultPresenter;
    private final SpotlightCommandQueryRouter commandQueryRouter;
    private final SpotlightItemQueryRouter itemQueryRouter;

    public SpotlightQueryRouter(InputWidget inputWidget, SpotlightResultPresenter resultPresenter, SpotlightCommandQueryRouter commandQueryRouter, SpotlightItemQueryRouter itemQueryRouter)
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
