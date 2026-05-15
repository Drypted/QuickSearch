package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.blueprints.commands.Command;
import com.drypted.spotlight.client.core.handlers.CommandsHandler;
import com.drypted.spotlight.client.core.input.CommandInputParser;
import com.drypted.spotlight.client.ui.components.InputWidget;

public final class SpotlightCommandQueryRouter
{
    private final InputWidget inputWidget;
    private final SpotlightResultPresenter resultPresenter;

    public SpotlightCommandQueryRouter(InputWidget inputWidget, SpotlightResultPresenter resultPresenter)
    {
        this.inputWidget = inputWidget;
        this.resultPresenter = resultPresenter;
    }

    public void route(String text)
    {
        String commandName = CommandInputParser.getCommandName(text);
        boolean hasSpaceAfterCommand = CommandInputParser.hasStartedArguments(text);

        Command command = CommandsHandler.getRawCommand(commandName);

        if (command != null && hasSpaceAfterCommand)
        {
            String[] args = CommandInputParser.getArgs(inputWidget.getText());
            resultPresenter.displayArgSuggestions(command, args);
            inputWidget.showError(command.validateArgs(args));
            return;
        }

        CommandsHandler.getCommands(text, resultPresenter::displayCommands);

        if (command != null)
        {
            inputWidget.showError(command.validateArgs(CommandInputParser.getArgs(inputWidget.getText())));
        }
    }
}
