package com.drypted.spotlight.client.core.commands.argument.types;

import com.drypted.spotlight.client.core.commands.CommandFeedback;
import com.drypted.spotlight.client.core.commands.argument.ArgumentParseException;
import com.drypted.spotlight.client.core.commands.argument.ArgumentType;
import com.drypted.spotlight.client.core.storage.HotbarStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Accepts a saved hotbar preset name. Provides live suggestions from stored hotbar names.
 */
public class SavedHotbarArgumentType implements ArgumentType<String>
{
    private static final String PATTERN = "^[a-zA-Z]+$";

    @Override
    public String parse(String raw) throws ArgumentParseException
    {
        if (raw == null || raw.isBlank())
            throw new ArgumentParseException("Please enter a hotbar name");
        String trimmed = raw.trim();
        if (!trimmed.matches(PATTERN))
            throw new ArgumentParseException("Please input only alphabets");
        return trimmed.toLowerCase();
    }

    @Override
    public CommandFeedback validate(String raw)
    {
        if (raw == null || raw.isBlank())
            return CommandFeedback.withError("Please enter a hotbar name");
        if (!raw.trim().matches(PATTERN))
            return CommandFeedback.withError("Please input only alphabets");
        return CommandFeedback.NO_ERROR;
    }

    @Override
    public List<String> getSuggestions(String partial)
    {
        try
        {
            Set<String> names = HotbarStorage.getStoredNames();
            if (partial == null || partial.isBlank())
            {
                return new ArrayList<>(names);
            }

            String lower = partial.toLowerCase().trim();
            List<String> matches = new ArrayList<>();
            for (String name : names)
            {
                if (name.toLowerCase().startsWith(lower))
                {
                    matches.add(name);
                }
            }
            return matches;
        }
        catch (IOException e)
        {
            return List.of();
        }
    }

    @Override
    public String getUsageHint()
    {
        return "<hotbar_name>";
    }
}

