package com.drypted.quicksearch.client.core.blueprints.commands.argument.types;

import com.drypted.quicksearch.client.core.blueprints.commands.argument.ArgumentParseException;
import com.drypted.quicksearch.client.core.blueprints.commands.argument.ArgumentType;
import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;

import java.util.List;

/**
 * Accepts any non-empty string. No suggestions.
 */
public class StringArgumentType implements ArgumentType<String>
{
    private final String usageHint;

    public StringArgumentType(String usageHint)
    {
        this.usageHint = usageHint;
    }

    public StringArgumentType()
    {
        this("<text>");
    }

    @Override
    public String parse(String raw) throws ArgumentParseException
    {
        if (raw == null || raw.isBlank())
            throw new ArgumentParseException("Expected a non-empty string");
        return raw.trim();
    }

    @Override
    public CommandFeedback validate(String raw)
    {
        if (raw == null || raw.isBlank())
            return CommandFeedback.withError("Expected a non-empty string");
        return CommandFeedback.NO_ERROR;
    }

    @Override
    public List<String> getSuggestions(String partial)
    {
        return List.of();
    }

    @Override
    public String getUsageHint()
    {
        return usageHint;
    }
}

