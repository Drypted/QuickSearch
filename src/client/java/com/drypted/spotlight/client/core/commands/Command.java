package com.drypted.spotlight.client.core.commands;

import com.drypted.spotlight.client.core.search.Searchable;
import net.minecraft.client.player.LocalPlayer;

public interface Command extends Searchable
{
    boolean isNotArgs();

    String getName();

    String getDescription();

    CommandError validateArgs(String[] args);

    CommandError execute(String[] args, LocalPlayer player);

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
