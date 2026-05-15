package com.drypted.quicksearch.client.core.submit;

import com.drypted.quicksearch.client.core.actions.GiveItemAction;
import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.feedback.errors.InvalidItemError;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import com.drypted.quicksearch.client.ui.state.ViewState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ItemSubmitHandler
{
    private final InputWidget inputWidget;
    private final ViewState viewState;
    private final Runnable closeAction;

    public ItemSubmitHandler(InputWidget inputWidget, ViewState viewState, Runnable closeAction)
    {
        this.inputWidget = inputWidget;
        this.viewState = viewState;
        this.closeAction = closeAction;
    }

    public void submit()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemsResultData itemResult = this.viewState.getSubmitItemResult();
        if (itemResult == null)
        {
            this.inputWidget.showError(new InvalidItemError());
            return;
        }

        GiveItemAction.run(player, itemResult);
        closeAction.run();
    }
}
