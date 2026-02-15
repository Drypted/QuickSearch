package com.drypted.spotlight.client.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CommandsHandler
{
    private static final ArrayList<Command> commands = new ArrayList<>();

    static
    {
        commands.add(new TestCommand());
    }

    public static void getCommands(String query, Consumer<List<Command>> onComplete)
    {
        List<Command> results = new ArrayList<>();

        for (Command command : commands)
        {
            if (query.contains(command.getName()))
            {
                results.add(command);
            }
        }

        onComplete.accept(results);
    }
}
