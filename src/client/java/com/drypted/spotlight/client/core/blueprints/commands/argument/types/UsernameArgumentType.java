package com.drypted.spotlight.client.core.blueprints.commands.argument.types;

import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.commands.argument.ArgumentParseException;
import com.drypted.spotlight.client.core.blueprints.commands.argument.ArgumentType;

import java.util.List;

/**
 * Validates a Minecraft username (3-16 chars, alphanumeric + underscore). No suggestions.
 */
public class UsernameArgumentType implements ArgumentType<String>
{
    private static final String PATTERN = "^[a-zA-Z0-9_]{3,16}$";

    @Override
    public String parse(String raw) throws ArgumentParseException
    {
        if (raw == null || raw.isBlank())
            throw new ArgumentParseException("Please enter a username");
        String trimmed = raw.trim();
        if (!trimmed.matches(PATTERN))
            throw new ArgumentParseException("Please enter a valid username");
        return trimmed;
    }

    @Override
    public CommandFeedback validate(String raw)
    {
        if (raw == null || raw.isBlank())
            return CommandFeedback.withError("Please enter a username");
        if (!raw.trim().matches(PATTERN))
            return CommandFeedback.withError("Please enter a valid username");
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
        return "<username>";
    }
}

