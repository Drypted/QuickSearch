package com.drypted.quicksearch.client.core.submit;

import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.quicksearch.client.core.handlers.CommandsHandler;
import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class CommandSubmitHandler
{
    private final InputWidget inputWidget;
    private final Runnable closeAction;

    public CommandSubmitHandler(InputWidget inputWidget, Runnable closeAction)
    {
        this.inputWidget = inputWidget;
        this.closeAction = closeAction;
    }

    public void submit(String text)
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
                            .withStyle(error.getSeverity().getChatColor()),
                    false
            );
        }

        closeAction.run();
    }
}
