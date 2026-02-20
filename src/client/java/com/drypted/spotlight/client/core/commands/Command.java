package com.drypted.spotlight.client.core.commands;

import com.drypted.spotlight.client.core.search.Searchable;
import net.minecraft.client.player.LocalPlayer;

public interface Command extends Searchable
{
    boolean requiresArgs();

    String getName();

    String getDescription();

    CommandFeedback validateArgs(String[] args);

    CommandFeedback execute(String[] args, LocalPlayer player);

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
