package com.drypted.spotlight.client.core;

import com.drypted.spotlight.client.gui.SearchResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchHandler
{
    private static final List<SearchResultData> GAME_ITEMS;
    private static final int MAX_RESULTS = 60;

    // Keep track of the active search to allow cancellation
    private static CompletableFuture<Void> activeSearchTask;

    static
    {
        GAME_ITEMS = BuiltInRegistries.ITEM.stream().map(SearchResultData::fromItem).toList();
    }

    public static List<SearchResultData> getGameItems()
    {
        return GAME_ITEMS;
    }

    /**
     * Performs an asynchronous search.
     * Cancels any currently running search, filters the data on a background thread,
     * and executes the callback on the Minecraft Main Thread.
     *
     * @param query      The text to search for.
     * @param onComplete Callback to run on the main thread with the results.
     */
    public static void searchAsync(String query, Consumer<List<SearchResultData>> onComplete)
    {
        // 1. Cancel existing search if it's still running
        if (activeSearchTask != null && !activeSearchTask.isDone())
        {
            activeSearchTask.cancel(true);
        }

        // 2. Handle empty queries immediately
        if (query == null || query.isBlank())
        {
            onComplete.accept(Collections.emptyList());
            return;
        }

        // 3. Run search in background
        activeSearchTask = CompletableFuture.supplyAsync(() -> {
            // This runs in the common ForkJoinPool
            return GAME_ITEMS.stream()
                             .filter(item -> item.containsText(query))
                             .limit(MAX_RESULTS)
                             .collect(Collectors.toList());
        }).thenAcceptAsync(results -> {
            // 4. Return results to the Main Thread safely
            Minecraft.getInstance().execute(() -> onComplete.accept(results));
        }).exceptionally(e -> {
            // Handle cancellation (CompletionException wrapping CancellationException)
            return null;
        });
    }
}