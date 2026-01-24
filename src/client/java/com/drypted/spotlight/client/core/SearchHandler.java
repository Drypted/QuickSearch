package com.drypted.spotlight.client.core;

import com.drypted.spotlight.client.gui.SearchResultData;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.stream.Collectors;

public class SearchHandler
{
    private static final List<SearchResultData> GAME_ITEMS;
    private static final int maxResults = 60;

    static
    {
        GAME_ITEMS = BuiltInRegistries.ITEM.stream().map(SearchResultData::fromItem).toList();
    }

    public static List<SearchResultData> getGameItems()
    {
        return GAME_ITEMS;
    }

    public static List<SearchResultData> search(String query)
    {
        if (query == null || query.isEmpty())
            return List.of();

        return GAME_ITEMS.stream().filter(item -> item.containsText(query)).limit(maxResults).collect(Collectors.toList());
    }
}
