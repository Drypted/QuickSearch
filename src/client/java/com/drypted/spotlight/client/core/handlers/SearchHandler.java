package com.drypted.spotlight.client.core.handlers;

import com.drypted.spotlight.client.SpotlightClient;
import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import com.drypted.spotlight.client.core.search.SimpleSearch;
import com.drypted.spotlight.client.core.search.SmartSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SearchHandler
{
    private static final Supplier<Integer> MAX_RESULTS_SUPPLIER = () -> SpotlightClient.getConfig().search.maxResults;
    private static final Supplier<SearchMode> SEARCH_MODE_SUPPLIER = () -> //
            SpotlightClient.getConfig().search.fuzzySearch ? SearchMode.SMART : SearchMode.SIMPLE;

    private static List<ItemsResultData> GameItems = Collections.emptyList();

    private static SimpleSearch<ItemsResultData> simpleSearch = new SimpleSearch<>(GameItems);
    private static SmartSearch<ItemsResultData> smartSearch;

    // Keep track of the active search to allow cancellation
    private static CompletableFuture<Void> ActiveSearchTask;

    public static void rebuildGameItems()
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null)
        {
            GameItems = Collections.emptyList();
            smartSearch = new SmartSearch<ItemsResultData>(GameItems);
            return;
        }

        LinkedHashMap<String, ItemsResultData> combined = new LinkedHashMap<>();

        // 1. Creative-mode items (sorted, visible)
        CreativeModeTabs.allTabs().stream().flatMap(tab -> tab.getDisplayItems().stream()).forEach(stack -> {
            ItemsResultData data = ItemsResultData.fromItemStack(stack);
            combined.putIfAbsent(data.getSerializedDefinition(), data);
        });

        // 2. Registry fallback (includes hidden mod items)
        BuiltInRegistries.ITEM.stream().forEach(item -> {
            try
            {
                ItemsResultData data = ItemsResultData.fromItem(item);
                combined.putIfAbsent(data.getSerializedDefinition(), data);
            }
            catch (Exception ignored)
            {
                // Some items are not safe to instantiate
            }
        });

        GameItems = List.copyOf(combined.values());

        // Rebuild/replace smart search instance with the new items
        simpleSearch = new SimpleSearch<ItemsResultData>(GameItems);
        smartSearch = new SmartSearch<ItemsResultData>(GameItems);
    }

    public static void requestCreativeTabRebuild()
    {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;

        if (level == null || player == null)
        {
            return;
        }

        CreativeModeTabs.tryRebuildTabContents(
                level.enabledFeatures(),
                player.canUseGameMasterBlocks(),
                level.registryAccess()
        );
    }

    /**
     * Performs an asynchronous search using the default search mode (simple search by default).
     *
     * @param query      The text to search for.
     * @param onComplete Callback to run on the main thread with the results.
     */
    public static void searchAsync(String query, Consumer<List<ItemsResultData>> onComplete)
    {
        searchAsync(query, onComplete, SEARCH_MODE_SUPPLIER.get());
    }

    /**
     * Performs an asynchronous search. Cancels any currently running search, filters the data on a background thread,
     * and executes the callback on the Minecraft Main Thread.
     *
     * @param query      The text to search for.
     * @param onComplete Callback to run on the main thread with the results.
     * @param mode       The search mode to use (simple or smart).
     */
    public static void searchAsync(String query, Consumer<List<ItemsResultData>> onComplete, SearchMode mode)
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
        ActiveSearchTask = CompletableFuture.supplyAsync(() -> switch (SEARCH_MODE_SUPPLIER.get())
        {
            case SMART ->
            {
                // Ensure smartSearch exists
                if (smartSearch == null)
                {
                    smartSearch = new SmartSearch<ItemsResultData>(GameItems);
                }

                // SmartSearch.search returns a Stream; collect up to MAX_RESULTS
                yield smartSearch.search(query, MAX_RESULTS_SUPPLIER.get()).collect(Collectors.toList());
            }
            case null, default ->
            {
                if (simpleSearch == null)
                {
                    simpleSearch = new SimpleSearch<>(GameItems);
                }

                yield simpleSearch.search(query, MAX_RESULTS_SUPPLIER.get());
            }
        }).thenAcceptAsync(results -> {
            // 4. Return results to the Main Thread safely
            Minecraft.getInstance().execute(() -> onComplete.accept(results));
        }).exceptionally(e -> {
            // Handle cancellation (CompletionException wrapping CancellationException) and other errors
            return null;
        });
    }

    /* GETTERS & SETTERS */

    public static List<ItemsResultData> getGameItems()
    {
        return GameItems;
    }

    public static SmartSearch<ItemsResultData> getSmartSearchInstance()
    {
        return smartSearch;
    }

    /* HELPERS CLASSES & ENUMS */

    /**
     * Normalizes a string for more consistent searching. This includes:
     *
     * <li>Trimming leading/trailing whitespace</li>
     * <li>Converting to lowercase (case-insensitive)</li>
     * <li>Replacing underscores with spaces (treating them as equivalent)</li>
     * <li>Collapsing multiple consecutive whitespace characters into a single space</li>
     *
     * @param input The string to normalize / Unsanitized string
     *
     * @return A normalized version of the input string, or an empty string if the input is null
     */
    public static String normalizeString(String input)
    {
        if (input == null) return "";
        return input.strip() // remove leading/trailing whitespace
                .toLowerCase() // case-insensitive
                .replace('_', ' ') // treat underscores as spaces
                .replaceAll("\\s+", " "); // collapse multiple spaces into one
    }

    public enum SearchMode
    {
        SIMPLE,
        SMART
    }
}