package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.actions.GiveItemAction;
import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import com.drypted.spotlight.client.core.blueprints.feedback.errors.InvalidItemError;
import com.drypted.spotlight.client.ui.components.InputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class SpotlightSubmitItemHandler
{
    private final InputWidget inputWidget;
    private final SpotlightViewState viewState;
    private final Runnable closeAction;

    public SpotlightSubmitItemHandler(InputWidget inputWidget, SpotlightViewState viewState, Runnable closeAction)
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
