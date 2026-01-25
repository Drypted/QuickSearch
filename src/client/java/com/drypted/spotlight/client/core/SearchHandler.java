package com.drypted.spotlight.client.core;

import com.drypted.spotlight.client.gui.SearchResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchHandler
{
    private static final int MAX_RESULTS = 60;

    private static boolean RequestedOnce = false;
    private static List<SearchResultData> GameItems = Collections.emptyList();

    // Keep track of the active search to allow cancellation
    private static CompletableFuture<Void> ActiveSearchTask;

    public static void rebuildGameItems()
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null)
        {
            GameItems = Collections.emptyList();
            return;
        }

        CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters( //
                minecraft.level.enabledFeatures(), true, minecraft.level.registryAccess());

        // Match CreativeModeTabs.buildAllTabContents exactly
        CreativeModeTabs.allTabs()
                        .stream()
                        .filter(tab -> tab.getType() == CreativeModeTab.Type.CATEGORY)
                        .forEach(tab -> tab.buildContents(params));

        CreativeModeTabs.allTabs()
                        .stream()
                        .filter(tab -> tab.getType() != CreativeModeTab.Type.CATEGORY)
                        .forEach(tab -> tab.buildContents(params));

        GameItems = CreativeModeTabs.allTabs().stream().flatMap(tab -> tab.getDisplayItems()
                                                                          .stream()).distinct().map(
                SearchResultData::fromItemStack).toList();
    }

    public static void requestCreativeTabRebuild()
    {
        if (RequestedOnce)
        {
            return;
        }

        RequestedOnce = true;

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null)
        {
            return;
        }

        CreativeModeTabs.tryRebuildTabContents(
                minecraft.level.enabledFeatures(),
                true,
                minecraft.level.registryAccess()
        );
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
        if (ActiveSearchTask != null && !ActiveSearchTask.isDone())
        {
            ActiveSearchTask.cancel(true);
        }

        // 2. Handle empty queries immediately
        if (query == null || query.isBlank())
        {
            onComplete.accept(Collections.emptyList());
            return;
        }

        // 3. Run search in background
        ActiveSearchTask = CompletableFuture.supplyAsync(() -> {
            // This runs in the common ForkJoinPool
            return GameItems.stream()
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

    /* Getters */

    public static List<SearchResultData> getGameItems()
    {
        return GameItems;
    }
}