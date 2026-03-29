package com.drypted.spotlight.client.core.blueprints.commands.argument.types;

import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.commands.argument.ArgumentParseException;
import com.drypted.spotlight.client.core.blueprints.commands.argument.ArgumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Accepts a saved preset name. Provides live suggestions from stored preset names.
 */
public class StringOptionArgumentType implements ArgumentType<String>
{
    private static final String PATTERN = "^[a-zA-Z]+$";
    private final Supplier<Set<String>> options;

    public StringOptionArgumentType(Supplier<Set<String>> options)
    {
        this.options = options;
    }

    @Override
    public String parse(String raw) throws ArgumentParseException
    {
        if (raw == null || raw.isBlank()) throw new ArgumentParseException("Please enter a preset name");
        String trimmed = raw.trim();
        if (!trimmed.matches(PATTERN)) throw new ArgumentParseException("Please input only alphabets");
        return trimmed.toLowerCase();
    }

    @Override
    public CommandFeedback validate(String raw)
    {
        if (raw == null || raw.isBlank()) return CommandFeedback.withError("Please enter a preset name");
        if (!raw.trim().matches(PATTERN)) return CommandFeedback.withError("Please input only alphabets");
        return CommandFeedback.NO_ERROR;
    }

    @Override
    public List<String> getSuggestions(String partial)
    {
        if (partial == null || partial.isBlank())
        {
            return new ArrayList<>(options.get());
        }

        String lower = partial.toLowerCase().trim();
        List<String> matches = new ArrayList<>();
        for (String name : options.get())
        {
            if (name.toLowerCase().startsWith(lower))
            {
                matches.add(name);
            }
        }
        return matches;
    }

    @Override
    public String getUsageHint()
    {
        return "<name>";
    }
}

