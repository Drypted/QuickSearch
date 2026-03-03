package com.drypted.spotlight.client.core.blueprints.search;

/**
 * Interface for objects that can be searched. Implement this interface to make any type searchable with SmartSearch.
 */
public interface Searchable
{
    /**
     * Returns the primary display name used for searching. This is typically what users see and is given highest
     * priority in matching.
     */
    String getPrimaryQuery();

    /**
     * Returns the identifier/path used for searching. This is typically a unique key or technical name, given lower
     * priority than display name.
     */
    String getSecondaryQuery();
}