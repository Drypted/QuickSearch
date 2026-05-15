package com.drypted.spotlight.client.core.handlers;

import com.drypted.spotlight.client.core.blueprints.commands.Command;
import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.search.SmartSearch;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class CommandRegistry
{
    private static final int COMMAND_SEARCH_LIMIT = 10;

    private final Map<String, Command> commandByName = new HashMap<>();
    private SmartSearch<Command> commandSearch = new SmartSearch<>(new ArrayList<>());

    void register(Command command)
    {
        commandByName.put(command.getName().toLowerCase(), command);
    }

    void rebuildIndex()
    {
        commandSearch = new SmartSearch<>(new ArrayList<>(commandByName.values()));
    }

    List<Command> searchByUserInput(String userInput)
    {
        if (userInput == null || userInput.length() <= 1)
        {
            return new ArrayList<>(commandByName.values());
        }

        String commandName = userInput.substring(1).split("\\s+")[0];
        return commandSearch.search(commandName, COMMAND_SEARCH_LIMIT).collect(Collectors.toList());
    }

    @Nullable Command getRawCommand(String name)
    {
        if (name == null) return null;
        return commandByName.get(name.toLowerCase());
    }

    CommandFeedback execute(String name, String[] args, LocalPlayer player)
    {
        Command command = getRawCommand(name);
        if (command != null)
        {
            return command.execute(args, player);
        }

        return CommandFeedback.withError("Please enter a valid command name!");
    }

    List<String> getArgSuggestions(String commandName, String[] args)
    {
        Command command = getRawCommand(commandName);
        if (command == null) return List.of();
        return command.getSuggestions(args);
    }
}