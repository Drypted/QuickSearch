package com.drypted.spotlight.client.core.algorithms;

import com.drypted.spotlight.client.models.SearchResultData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SimpleSearch
{
    private SimpleSearch() { }

    /**
     * Simple, fast substring-based search (keeps previous behavior).
     *
     * @param items      list to search
     * @param query      query text
     * @param maxResults maximum results to return
     *
     * @return matched results (up to maxResults)
     */
    public static List<SearchResultData> search(List<SearchResultData> items, String query, int maxResults)
    {
        if (query == null || query.isBlank() || items == null || items.isEmpty())
        {
            return Collections.emptyList();
        }

        return items.stream().filter(item -> item.containsText(query)).limit(maxResults).collect(
                Collectors.toList());
    }
}