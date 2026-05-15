package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.handlers.CommandsHandler;
import com.drypted.spotlight.client.core.input.CommandInputParser;
import com.drypted.spotlight.client.ui.components.InputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class SpotlightSubmitCommandHandler
{
    private final InputWidget inputWidget;
    private final Runnable closeAction;

    public SpotlightSubmitCommandHandler(InputWidget inputWidget, Runnable closeAction)
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
