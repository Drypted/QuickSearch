package com.drypted.quicksearch.client.core.blueprints.commands;

import com.drypted.quicksearch.client.core.blueprints.commands.argument.ArgumentType;
import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base for commands that declare typed arguments via {@link ArgumentType}. Provides automatic validation,
 * suggestion generation, and usage string building.
 */
public abstract class ArgumentedCommand implements Command
{
    private final List<ArgumentType<?>> argumentTypes;

    protected ArgumentedCommand(ArgumentType<?>... types)
    {
        List<ArgumentType<?>> list = new ArrayList<>();
        Collections.addAll(list, types);
        this.argumentTypes = Collections.unmodifiableList(list);
    }

    /**
     * @return The ordered list of argument type definitions for this command.
     */
    public List<ArgumentType<?>> getArgumentTypes() { return argumentTypes; }

    @Override
    public boolean requiresArgs() { return !argumentTypes.isEmpty(); }

    /**
     * Builds the usage string, e.g. {@code /head <username>}
     */
    public String getUsage()
    {
        if (argumentTypes.isEmpty()) return "/" + getName();
        return "/" + getName() + " " + argumentTypes.stream()
                                                    .map(ArgumentType::getUsageHint)
                                                    .collect(Collectors.joining(" "));
    }

    @Override
    public CommandFeedback validateArgs(String[] args)
    {
        if (args.length == 0 && !argumentTypes.isEmpty())
        {
            return CommandFeedback.withError("Usage: " + getUsage());
        }

        // Validate each provided argument against its type
        int count = Math.min(args.length, argumentTypes.size());
        for (int i = 0; i < count; i++)
        {
            CommandFeedback feedback = argumentTypes.get(i).validate(args[i]);
            if (feedback.haltsExecution()) return feedback;
        }

        // Too many arguments
        if (args.length > argumentTypes.size())
        {
            return CommandFeedback.withWarning("Too many arguments. Usage: " + getUsage());
        }

        return CommandFeedback.NO_ERROR;
    }

    @Override
    public List<String> getSuggestions(String[] args)
    {
        if (argumentTypes.isEmpty()) return List.of();

        // Determine which argument slot we're currently typing
        // args may be empty (no arg typed yet), or args[i] is the current partial for slot i
        int currentSlot;
        String partial;

        if (args.length == 0)
        {
            // User typed the command name but no space-separated arg yet
            currentSlot = 0;
            partial = "";
        }
        else
        {
            // The last element in args is the current partial input
            currentSlot = args.length - 1;
            partial = args[currentSlot];
        }

        // If the current slot is beyond our defined argument types, no suggestions
        if (currentSlot >= argumentTypes.size()) return List.of();

        return argumentTypes.get(currentSlot).getSuggestions(partial);
    }
}

