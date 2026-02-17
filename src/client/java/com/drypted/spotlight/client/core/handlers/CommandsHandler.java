package com.drypted.spotlight.client.core.handlers;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.TestArgsCommand;
import com.drypted.spotlight.client.core.commands.TestNoArgsCommand;
import com.drypted.spotlight.client.core.search.SmartSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandsHandler
{
    private static final ArrayList<Command> commands = new ArrayList<>();
    private static SmartSearch<Command> commandSearch;

    static
    {
        commands.add(new TestNoArgsCommand());
        commands.add(new TestArgsCommand());

        // Build search index
        commandSearch = new SmartSearch<>(commands);
    }

    /**
     * Searches for commands matching the user input.
     *
     * @param userInput  The user's input (should start with "/")
     * @param onComplete Callback with the matching commands
     */
    public static void getCommands(String userInput, Consumer<List<Command>> onComplete)
    {
        if (userInput == null || userInput.length() <= 1)
        {
            onComplete.accept(commands); // Show all commands if input is empty or just "/"
            return;
        }

        // Split by one or more whitespace characters
        String commandName = userInput.substring(1).split("\\s+")[0];

        List<Command> results = commandSearch.search(commandName, 10).collect(Collectors.toList());

        onComplete.accept(results);
    }

    /**
     * Rebuilds the commands search index. Call this when commands are added/removed.
     */
    public static void rebuildCommandIndex()
    {
        commandSearch = new SmartSearch<>(commands);
    }
}