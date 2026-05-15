package com.drypted.spotlight.client.core.blueprints.commands.argument.types;

import com.drypted.spotlight.client.core.blueprints.commands.argument.ArgumentParseException;
import com.drypted.spotlight.client.core.blueprints.commands.argument.ArgumentType;
import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;

import java.util.List;

/**
 * Accepts only alphabetic words (a-zA-Z). No suggestions by default.
 */
public class WordArgumentType implements ArgumentType<String>
{
    private static final String PATTERN = "^[a-zA-Z]+$";

    private final String usageHint;

    public WordArgumentType(String usageHint)
    {
        this.usageHint = usageHint;
    }

    public WordArgumentType()
    {
        this("<word>");
    }

    @Override
    public String parse(String raw) throws ArgumentParseException
    {
        if (raw == null || raw.isBlank())
            throw new ArgumentParseException("Expected alphabetic word");
        String trimmed = raw.trim();
        if (!trimmed.matches(PATTERN))
            throw new ArgumentParseException("Please input only alphabets");
        return trimmed;
    }

    @Override
    public CommandFeedback validate(String raw)
    {
        if (raw == null || raw.isBlank())
            return CommandFeedback.withError("Expected alphabetic word");
        if (!raw.trim().matches(PATTERN))
            return CommandFeedback.withError("Please input only alphabets");
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

