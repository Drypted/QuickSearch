package com.drypted.spotlight.client.core.search;

import com.drypted.spotlight.client.core.blueprints.search.Searchable;
import com.drypted.spotlight.client.core.handlers.SearchHandler;

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
     * @param unsanitizedQuery query text (unsanitized, may contain leading/trailing whitespace, case-insensitive)
     * @param maxResults       maximum results to return
     *
     * @return matched results (up to maxResults)
     */
    public List<T> search(final String unsanitizedQuery, final int maxResults)
    {
        if (unsanitizedQuery == null || unsanitizedQuery.isBlank() || items == null || items.isEmpty())
        {
            return Collections.emptyList();
        }

        return items.stream() //
                .filter(item -> isMatch(item, unsanitizedQuery)) //
                .limit(maxResults) //
                .collect(Collectors.toList());
    }

    private static <T extends Searchable> boolean isMatch(T item, String unsanitizedQuery)
    {
        String query = SearchHandler.normalizeString(unsanitizedQuery);
        return SearchHandler.normalizeString(item.getPrimaryQuery()).contains(query) //
                || SearchHandler.normalizeString(item.getSecondaryQuery()).contains(query);
    }
}