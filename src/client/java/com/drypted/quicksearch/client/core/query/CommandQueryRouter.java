package com.drypted.quicksearch.client.core.query;

import com.drypted.quicksearch.client.core.blueprints.commands.Command;
import com.drypted.quicksearch.client.core.handlers.CommandsHandler;
import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.core.result.ResultPresenter;
import com.drypted.quicksearch.client.ui.components.InputWidget;

public final class CommandQueryRouter
{
    private final InputWidget inputWidget;
    private final ResultPresenter resultPresenter;

    public CommandQueryRouter(InputWidget inputWidget, ResultPresenter resultPresenter)
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
