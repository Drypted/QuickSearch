package com.drypted.spotlight.client.core.handlers;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
import com.drypted.spotlight.client.core.commands.args.GetPlayerHeadCommand;
import com.drypted.spotlight.client.core.commands.args.LoadHotbarCommand;
import com.drypted.spotlight.client.core.commands.args.SaveHotbarCommand;
import com.drypted.spotlight.client.core.commands.args.TemplateArgsCommand;
import com.drypted.spotlight.client.core.commands.no_args.TemplateNoArgsCommand;
import com.drypted.spotlight.client.core.search.SmartSearch;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandsHandler
{
    /* REGISTRY BEGIN */
    private static final Map<String, Command> REGISTRY = new HashMap<>();

    private static void register(Command cmd)
    {
        REGISTRY.put(cmd.getName().toLowerCase(), cmd);
    }

    /* REGISTRY END */

    private static SmartSearch<Command> commandSearch;

    static
    {
        // args
        register(new GetPlayerHeadCommand());
        register(new SaveHotbarCommand());
        try {register(new LoadHotbarCommand());} catch (IOException ignored) {}
        // no args

        rebuildCommandIndex();
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
            onComplete.accept(new ArrayList<>(REGISTRY.values())); // Show all commands if input is empty or just "/"
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
        commandSearch = new SmartSearch<>(new ArrayList<>(REGISTRY.values()));
    }


    /* PUBLIC COMMAND INTERFACE */

    public static @Nullable Command getRawCommand(String name)
    {
        return REGISTRY.get(name.toLowerCase());
    }

    public static CommandFeedback execute(String name, String[] args, LocalPlayer player)
    {
        Command cmd = REGISTRY.get(name.toLowerCase());
        if (cmd != null)
        {
            return cmd.execute(args, player);
        }

        return CommandFeedback.withError("Please enter a valid command name!");
    }

    /**
     * Returns argument suggestions for the currently active command based on what the user has typed.
     *
     * @param commandName The command name (without leading "/")
     * @param args        The arguments typed so far
     *
     * @return A list of suggestion strings for the current argument slot, or empty list
     */
    public static List<String> getArgSuggestions(String commandName, String[] args)
    {
        Command cmd = REGISTRY.get(commandName.toLowerCase());
        if (cmd == null) return List.of();
        return cmd.getSuggestions(args);
    }
}