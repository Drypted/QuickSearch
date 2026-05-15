package com.drypted.spotlight.client.core.blueprints.commands;

import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.search.Searchable;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public interface Command extends Searchable
{
    boolean requiresArgs();

    String getName();

    String getDescription();

    CommandFeedback validateArgs(String[] args);

    CommandFeedback execute(String[] args, LocalPlayer player);

    /// Shown when user types the command
    default String getUsage()
    {
        return "/" + getName();
    }

    /**
     * Provide argument suggestions based on the currently typed arguments.
     *
     * @param args The arguments the user has typed so far (may be empty)
     *
     * @return A list of suggestion strings for the current argument slot
     */
    default List<String> getSuggestions(String[] args)
    {
        return List.of();
    }

    /* SEARCHABLE DEFAULT IMPLEMENTATION */

    @Override
    default String getPrimaryQuery()
    {
        return getName();
    }

    @Override
    default String getSecondaryQuery()
    {
        return getDescription();
    }
}
