package com.drypted.quicksearch.client.ui.handlers;

import com.drypted.quicksearch.client.core.actions.GiveItemAction;
import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.quicksearch.client.core.blueprints.feedback.errors.InvalidItemError;
import com.drypted.quicksearch.client.core.handlers.CommandsHandler;
import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import com.drypted.quicksearch.client.ui.state.ResultPresenter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class SubmitHandler
{
    private final InputWidget inputWidget;
    private final ResultPresenter resultPresenter;
    private final Runnable screenCloseLambda;

    public SubmitHandler(InputWidget inputWidget, ResultPresenter resultPresenter, Runnable screenCloseLambda)
    {
        this.inputWidget = inputWidget;
        this.resultPresenter = resultPresenter;
        this.screenCloseLambda = screenCloseLambda;
    }

    public void submit(String text, boolean isCommandInput)
    {
        if (isCommandInput) submitCommand(text);
        else submitItem();
    }

    /* SUBMIT HANDLERS */

    public void submitCommand(String text)
    {
        String commandName = CommandInputParser.getCommandName(text);
        String[] args = CommandInputParser.getArgs(inputWidget.getText());

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        CommandFeedback error = CommandsHandler.execute(commandName, args, player);
        if (error.haltsExecution())
        {
            this.inputWidget.showError(error);
            return;
        }

        if (!error.getMessage().isEmpty())
        {
            player.displayClientMessage(
                    Component.literal(error.getSeverity().getName() + ": " + error.getMessage())
                             .withStyle(error.getSeverity().getChatColor()), false
            );
        }

        screenCloseLambda.run();
    }

    public void submitItem()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemsResultData itemResult = this.resultPresenter.getSubmitItemResult();
        if (itemResult == null)
        {
            this.inputWidget.showError(new InvalidItemError());
            return;
        }

        GiveItemAction.run(player, itemResult);
        screenCloseLambda.run();
    }
}
