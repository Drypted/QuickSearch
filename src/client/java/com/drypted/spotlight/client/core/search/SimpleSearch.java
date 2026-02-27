package com.drypted.spotlight.client.core.search;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SimpleSearch<T extends Searchable>
{
    private final List<T> items;

    public SimpleSearch(List<T> items)
    {
        this.items = items;
    }

    /**
     * Simple, fast substring-based search (keeps previous behavior).
     *
     * @param query      query text
     * @param maxResults maximum results to return
     *
     * @return matched results (up to maxResults)
     */
    public List<T> search(String query, int maxResults)
    {
        if (query == null || query.isBlank() || items == null || items.isEmpty())
        {
            return Collections.emptyList();
        }

        return items.stream().filter(item -> {
            return item.getPrimaryQuery().contains(query) || item.getSecondaryQuery().contains(query);
        }).limit(maxResults).collect(Collectors.toList());
    }
}