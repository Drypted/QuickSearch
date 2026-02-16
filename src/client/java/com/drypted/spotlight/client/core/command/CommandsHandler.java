package com.drypted.spotlight.client.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CommandsHandler
{
    private static final ArrayList<Command> commands = new ArrayList<>();

    static
    {
        commands.add(new TestNoArgsCommand());
        commands.add(new TestArgsCommand());
    }

    public static void getCommands(String userInput, Consumer<List<Command>> onComplete)
    {
        userInput = userInput.substring(1); // Remove the leading "/"
        List<Command> results = new ArrayList<>();

        // Get the command name part of the userInput
        String normalizedQuery = userInput.split(" ")[0].toLowerCase();

        for (Command command : commands)
        {
            String normalizedName = command.getName().toLowerCase();

            if (normalizedName.contains(normalizedQuery))
            {
                results.add(command);
            }
        }

        onComplete.accept(results);
    }
}
