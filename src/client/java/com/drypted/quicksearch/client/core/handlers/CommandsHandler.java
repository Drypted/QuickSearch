package com.drypted.quicksearch.client.core.handlers;

import com.drypted.quicksearch.client.core.blueprints.commands.Command;
import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.quicksearch.client.core.commands.args.*;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class CommandsHandler
{
    private static final CommandRegistry REGISTRY = new CommandRegistry();

    private static void register(Command cmd)
    {
        REGISTRY.register(cmd);
    }

    static
    {
        // args
        register(new GetPlayerHeadCommand());
        register(new SaveHotbarCommand());
        register(new SaveInventoryCommand());
        try { register(new LoadHotbarCommand()); } catch (IOException ignored) { }
        try { register(new LoadInventoryCommand()); } catch (IOException ignored) { }
        try { register(new DeleteHotbarCommand()); } catch (IOException ignored) { }
        try { register(new DeleteInventoryCommand()); } catch (IOException ignored) { }
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
        onComplete.accept(REGISTRY.searchByUserInput(userInput));
    }

    /**
     * Rebuilds the commands search index. Call this when commands are added/removed.
     */
    public static void rebuildCommandIndex()
    {
        REGISTRY.rebuildIndex();
    }


    /* PUBLIC COMMAND INTERFACE */

    public static @Nullable Command getRawCommand(String name)
    {
        return REGISTRY.getRawCommand(name);
    }

    public static CommandFeedback execute(String name, String[] args, LocalPlayer player)
    {
        return REGISTRY.execute(name, args, player);
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
        return REGISTRY.getArgSuggestions(commandName, args);
    }
}