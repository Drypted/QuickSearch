package com.drypted.spotlight.client.core.search;

import com.drypted.spotlight.client.core.blueprints.search.Searchable;
import com.drypted.spotlight.client.core.handlers.SearchHandler;

import java.util.*;
import java.util.stream.Stream;

/**
 * A comprehensive search system that implements multiple matching strategies to find relevant items.
 * <p>
 * This class provides intelligent search capabilities including: - Exact string matching - Prefix matching (e.g., "dia"
 * matches "diamond") - Fuzzy matching to handle typos (e.g., "dimond" matches "diamond") - Trigram-based similarity for
 * partial matches - Subsequence matching (e.g., "dmnd" matches "diamond")
 * <p>
 * The search uses inverted indices and trigram indices for efficient candidate selection, then scores each candidate to
 * rank results by relevance.
 *
 * @param <T> The type of searchable items (must implement {@link Searchable})
 */
public class SmartSearch<T extends Searchable>
{
    /* CONSTANTS */

    /**
     * Maximum allowed edit distance for fuzzy matching using Levenshtein distance. An edit distance of 3 means up to 3
     * character changes (insertions, deletions, substitutions) are tolerated when matching. For example: "dimond" →
     * "diamond" has distance 1.
     */
    private static final int MAXIMUM_FUZZY_EDIT_DISTANCE = 3;

    /**
     * Minimum similarity score (0.0 to 1.0) required for trigram-based matching. A trigram is a sequence of 3
     * consecutive characters. This threshold determines how similar two strings must be based on their shared trigrams.
     * 0.3 means at least 30% of trigrams must match.
     */
    private static final double MINIMUM_TRIGRAM_SIMILARITY_THRESHOLD = 0.3;

    /**
     * Maximum prefix length to index for efficient prefix lookups. Indexing prefixes up to 3 characters provides good
     * balance between memory usage and query performance for typical search queries.
     */
    private static final int MAXIMUM_PREFIX_LENGTH = 3;

    /* INSTANCE FIELDS */

    /**
     * The complete list of searchable items managed by this search instance. This list is immutable after being set via
     * constructor or rebuildIndices().
     */
    private List<T> searchableItems = Collections.emptyList();

    /**
     * Inverted index mapping individual words to the indices of items containing those words.
     * <p>
     * Example structure: "diamond" → [0, 5, 12]  (items at positions 0, 5, and 12 contain "diamond") "sword" → [3, 8,
     * 15]
     * <p>
     * This enables fast lookup of items containing specific words without scanning all items.
     */
    private Map<String, List<Integer>> wordToItemIndicesMap = Collections.emptyMap();

    /**
     * Prefix index mapping word prefixes (first 1-3 characters) to full words in the inverted index.
     * <p>
     * This enables efficient prefix matching without scanning all indexed words. For each word, we store mappings for
     * prefixes of length 1, 2, and 3.
     * <p>
     * Example structure: "d" → ["diamond", "door", "dirt"] "di" → ["diamond", "dirt"] "dia" → ["diamond"]
     * <p>
     * When searching for "dia", we can directly lookup matching words instead of scanning through all indexed words.
     * This reduces prefix matching from O(#words) to O(#words_with_prefix), a significant improvement for large
     * datasets.
     */
    private Map<String, Set<String>> prefixToWordsMap = Collections.emptyMap();

    /**
     * Trigram index mapping trigrams to the indices of items containing those trigrams.
     * <p>
     * A trigram is a sequence of 3 consecutive characters. For example, "diamond" contains: "  d", " di", "dia", "iam",
     * "amo", "mon", "ond", "nd ", "d  " (padded with spaces at start/end)
     * <p>
     * This index enables fuzzy matching by finding items with similar character sequences.
     */
    private Map<String, Set<Integer>> trigramToItemIndicesMap = Collections.emptyMap();

    /* CONSTRUCTORS */

    /**
     * Constructs a SmartSearch instance and builds search indices for the provided items.
     *
     * @param items The list of searchable items to index. If null, an empty list is used.
     */
    public SmartSearch(List<T> items)
    {
        if (items != null)
        {
            this.searchableItems = new ArrayList<>(items);
        }

        rebuildIndices(this.searchableItems);
    }

    /* PUBLIC API */

    /**
     * Replaces the current item list and rebuilds all search indices.
     * <p>
     * This is useful when the searchable dataset changes and you need to update the indices to reflect the new data.
     *
     * @param items The new list of items to search. If null, the search will be empty.
     */
    public void rebuildIndices(List<T> items)
    {
        if (items == null)
        {
            this.searchableItems = Collections.emptyList();
        }
        else
        {
            this.searchableItems = new ArrayList<>(items);
        }

        buildWordToItemIndicesMap();
        buildTrigramToItemIndicesMap();
    }

    /**
     * Searches for items matching the query and returns the best matches sorted by relevance.
     * <p>
     * The search process: 1. Finds candidate items using indices (inverted index and trigram index) 2. Scores each
     * candidate based on match quality (exact, prefix, fuzzy, etc.) 3. Returns items sorted by score (lower scores =
     * better matches)
     *
     * @param unsanitizedQuery query text (unsanitized, may contain leading/trailing whitespace, case-insensitive)
     * @param maximumResults   Maximum number of results to return. Use 0 or negative for unlimited.
     *
     * @return A stream of matching items, sorted by relevance (best matches first)
     */
    public Stream<T> search(String unsanitizedQuery, int maximumResults)
    {
        if (unsanitizedQuery == null)
        {
            return Stream.empty();
        }

        String sanitizedQuery = SearchHandler.normalizeString(unsanitizedQuery);

        // Step 1: Get candidate items that might match
        Set<Integer> candidateItemIndices = findCandidateItemIndices(sanitizedQuery);

        // If no candidates found through indices, try direct trigram similarity on all items
        if (candidateItemIndices.isEmpty())
        {
            for (int itemIndex = 0; itemIndex < searchableItems.size(); itemIndex++)
            {
                T item = searchableItems.get(itemIndex);

                double nameSimilarity = calculateTrigramSimilarity(
                        sanitizedQuery,
                        SearchHandler.normalizeString(item.getPrimaryQuery())
                );

                double pathSimilarity = calculateTrigramSimilarity(
                        sanitizedQuery,
                        SearchHandler.normalizeString(item.getSecondaryQuery())
                );

                double bestSimilarity = Math.max(nameSimilarity, pathSimilarity);

                // Use a higher threshold for direct comparison since we're checking everything
                if (bestSimilarity >= 0.4)
                {
                    candidateItemIndices.add(itemIndex);
                }
            }
        }

        // Step 2: Score candidates and filter out poor matches
        return candidateItemIndices.stream()
                .map(itemIndex -> {
                    T item = searchableItems.get(itemIndex);
                    ItemScoreResult scoreResult = calculateItemScore(item, sanitizedQuery);

                    // Filter out items with no match (score >= 1000)
                    if (scoreResult.totalScore() >= 1000)
                    {
                        return null;
                    }

                    return new SearchResultWithScore<>(item, scoreResult.totalScore(), itemIndex);
                })
                .filter(Objects::nonNull)
                // Step 3: Sort by score (lower is better), then by original position for stability
                .sorted(Comparator.comparingInt(SearchResultWithScore<T>::score)
                        .thenComparingInt(SearchResultWithScore::originalIndex))
                .limit(maximumResults > 0 ? maximumResults : Integer.MAX_VALUE)
                .map(SearchResultWithScore::item);
    }

    /* INDEX BUILDING */

    /**
     * Builds the inverted index that maps individual words to item indices.
     * <p>
     * An inverted index allows fast lookup of "which items contain this word?" without having to scan through all
     * items.
     * <p>
     * The index includes: - Individual words from item names (split on spaces, hyphens, underscores) - Individual words
     * from item paths (split on spaces, hyphens, underscores, slashes, colons, backslashes) - Full names and paths (for
     * exact matching)
     * <p>
     * Example: Item 0: name="Diamond Sword", path="items/diamond_sword" Index entries created: "diamond" → [0] "sword"
     * → [0] "items" → [0] "diamond sword" → [0] "items/diamond_sword" → [0]
     * <p>
     * Additionally builds a prefix index for efficient prefix matching (see prefixToWordsMap).
     */
    private void buildWordToItemIndicesMap()
    {
        Map<String, List<Integer>> indexMap = new HashMap<>();
        Map<String, Set<String>> prefixMap = new HashMap<>();

        for (int itemIndex = 0; itemIndex < searchableItems.size(); itemIndex++)
        {
            T item = searchableItems.get(itemIndex);

            // Index the identifier path (e.g., "minecraft:diamond_sword")
            // Split on common separators: underscore, hyphen, space, slash, colon, backslash
            String itemPath = SearchHandler.normalizeString(item.getSecondaryQuery());
            String[] pathWords = itemPath.split("[_\\-\\s/:\\\\]+");

            for (String word : pathWords)
            {
                if (!word.isEmpty())
                {
                    indexMap.computeIfAbsent(word, key -> new ArrayList<>()).add(itemIndex);
                    addWordToPrefixIndex(word, prefixMap);
                }
            }

            // Index the display name (e.g., "Diamond Sword")
            String displayName = SearchHandler.normalizeString(item.getPrimaryQuery());
            String[] nameWords = displayName.split("[_\\-\\s]+");

            for (String word : nameWords)
            {
                if (!word.isEmpty())
                {
                    indexMap.computeIfAbsent(word, key -> new ArrayList<>()).add(itemIndex);
                    addWordToPrefixIndex(word, prefixMap);
                }
            }

            // Also index the complete strings for exact matching
            indexMap.computeIfAbsent(itemPath, key -> new ArrayList<>()).add(itemIndex);
            indexMap.computeIfAbsent(displayName, key -> new ArrayList<>()).add(itemIndex);

            addWordToPrefixIndex(itemPath, prefixMap);
            addWordToPrefixIndex(displayName, prefixMap);
        }

        wordToItemIndicesMap = Collections.unmodifiableMap(indexMap);
        prefixToWordsMap = Collections.unmodifiableMap(prefixMap);
    }

    /**
     * Adds a word to the prefix index for all its prefixes (up to MAXIMUM_PREFIX_LENGTH).
     * <p>
     * For example, "diamond" will add: - "d" → ["diamond"] - "di" → ["diamond"] - "dia" → ["diamond"]
     * <p>
     * This allows fast lookup of words starting with a given prefix.
     *
     * @param word      The word to index
     * @param prefixMap The prefix map to update
     */
    private void addWordToPrefixIndex(String word, Map<String, Set<String>> prefixMap)
    {
        int maxLength = Math.min(word.length(), MAXIMUM_PREFIX_LENGTH);

        for (int prefixLength = 1; prefixLength <= maxLength; prefixLength++)
        {
            String prefix = word.substring(0, prefixLength);
            prefixMap.computeIfAbsent(prefix, key -> new HashSet<>()).add(word);
        }
    }

    /**
     * Builds the trigram index for fuzzy matching support.
     * <p>
     * A trigram is a sequence of 3 consecutive characters. For example: "cat" produces: "  c", " ca", "cat", "at ", "t
     * " (spaces are added as padding at the beginning and end)
     * <p>
     * Trigrams are useful for fuzzy matching because similar strings share many trigrams. For example: "diamond" and
     * "dimond" share most trigrams despite the typo "quick" and "quit" share fewer trigrams, indicating less
     * similarity
     * <p>
     * This index maps each trigram to all items containing that trigram, allowing efficient fuzzy search.
     */
    private void buildTrigramToItemIndicesMap()
    {
        Map<String, Set<Integer>> indexMap = new HashMap<>();

        for (int itemIndex = 0; itemIndex < searchableItems.size(); itemIndex++)
        {
            T item = searchableItems.get(itemIndex);

            // Generate trigrams from the item's path identifier
            String itemPath = SearchHandler.normalizeString(item.getSecondaryQuery());
            Set<String> pathTrigrams = generateTrigramsFromText(itemPath);

            for (String trigram : pathTrigrams)
            {
                indexMap.computeIfAbsent(trigram, key -> new HashSet<>()).add(itemIndex);
            }

            // Generate trigrams from the item's display name
            String displayName = SearchHandler.normalizeString(item.getPrimaryQuery());
            Set<String> nameTrigrams = generateTrigramsFromText(displayName);

            for (String trigram : nameTrigrams)
            {
                indexMap.computeIfAbsent(trigram, key -> new HashSet<>()).add(itemIndex);
            }
        }

        trigramToItemIndicesMap = Collections.unmodifiableMap(indexMap);
    }

    /* TRIGRAM OPERATIONS */

    /**
     * Generates all trigrams from the given text.
     * <p>
     * A trigram is a sequence of exactly 3 consecutive characters. This method adds padding (two spaces) at the
     * beginning and end of the text to capture edge trigrams.
     * <p>
     * Examples: "cat" → {"  c", " ca", "cat", "at ", "t  "} "go" → {"go"} (text shorter than 3 chars is returned as-is)
     * "test" → {"  t", " te", "tes", "est", "st ", "t  "}
     * <p>
     * The padding ensures that the first and last characters are properly weighted in similarity calculations.
     *
     * @param text The text to generate trigrams from
     *
     * @return A set of all trigrams found in the text
     */
    private Set<String> generateTrigramsFromText(String text)
    {
        Set<String> trigrams = new HashSet<>();

        // If text is shorter than 3 characters, return it as a single "trigram"
        if (text.length() < 3)
        {
            trigrams.add(text);
            return trigrams;
        }

        // Add padding: two spaces before and after
        // This ensures edge characters are captured in trigrams
        String paddedText = "  " + text + "  ";

        // Extract all consecutive 3-character sequences
        for (int position = 0; position < paddedText.length() - 2; position++)
        {
            trigrams.add(paddedText.substring(position, position + 3));
        }

        return trigrams;
    }

    /**
     * Calculates the similarity between two strings using trigram analysis.
     * <p>
     * This method uses the Jaccard similarity coefficient on trigram sets: similarity = (number of shared trigrams) /
     * (total number of unique trigrams)
     * <p>
     * The result ranges from 0.0 (completely different) to 1.0 (identical).
     * <p>
     * Examples: "diamond" vs "diamond" → 1.0 (identical) "diamond" vs "dimond" → ~0.7 (one letter different) "cat" vs
     * "dog" → ~0.0 (completely different)
     * <p>
     * This is useful for fuzzy matching as it's tolerant of small differences like typos.
     *
     * @param firstText  The first string to compare
     * @param secondText The second string to compare
     *
     * @return A similarity score between 0.0 and 1.0, where 1.0 means identical
     */
    private double calculateTrigramSimilarity(String firstText, String secondText)
    {
        Set<String> firstTrigrams = generateTrigramsFromText(firstText);
        Set<String> secondTrigrams = generateTrigramsFromText(secondText);

        // If both strings are empty, consider them identical
        if (firstTrigrams.isEmpty() && secondTrigrams.isEmpty())
        {
            return 1.0;
        }

        // Calculate intersection: trigrams present in both sets
        Set<String> sharedTrigrams = new HashSet<>(firstTrigrams);
        sharedTrigrams.retainAll(secondTrigrams);

        // Calculate union: all unique trigrams from both sets
        Set<String> allUniqueTrigrams = new HashSet<>(firstTrigrams);
        allUniqueTrigrams.addAll(secondTrigrams);

        // Jaccard similarity = |intersection| / |union|
        return allUniqueTrigrams.isEmpty() ? 0.0 : (double) sharedTrigrams.size() / allUniqueTrigrams.size();
    }

    /* FUZZY MATCHING - LEVENSHTEIN DISTANCE */

    /**
     * Calculates the Levenshtein distance between two strings with early termination.
     * <p>
     * The Levenshtein distance (also called edit distance) is the minimum number of single-character edits (insertions,
     * deletions, or substitutions) needed to change one string into another.
     * <p>
     * Examples: "cat" → "cat" : distance = 0 (identical) "cat" → "hat" : distance = 1 (substitute c→h) "cat" → "cats" :
     * distance = 1 (insert s) "saturday" → "sunday" : distance = 3
     * <p>
     * This implementation uses a space-optimized algorithm with two rolling rows instead of a full 2D matrix, reducing
     * memory usage from O(m×n) to O(n). It also supports early termination when the distance exceeds the maximum
     * threshold, further improving performance.
     * <p>
     * Time complexity: O(m × n) where m and n are the string lengths. Space complexity: O(n) where n is the length of
     * the second string.
     *
     * @param firstString     The first string
     * @param secondString    The second string
     * @param maximumDistance The maximum distance to calculate. If exceeded, returns Integer.MAX_VALUE.
     *
     * @return The minimum number of edits needed, or Integer.MAX_VALUE if it exceeds maximumDistance
     */
    private int calculateLevenshteinDistance(String firstString, String secondString, int maximumDistance)
    {
        // Quick optimization: if length difference exceeds max distance, no need to calculate
        if (Math.abs(firstString.length() - secondString.length()) > maximumDistance)
        {
            return Integer.MAX_VALUE;
        }

        // Use rolling arrays instead of full 2D matrix to save memory
        int[] previousRow = new int[secondString.length() + 1];
        int[] currentRow = new int[secondString.length() + 1];

        // Initialize first row: distance from empty string to secondString prefixes
        for (int j = 0; j <= secondString.length(); j++)
        {
            previousRow[j] = j;
        }

        // Fill the table row by row using dynamic programming
        for (int i = 1; i <= firstString.length(); i++)
        {
            currentRow[0] = i;  // Distance from empty string to firstString prefix
            int minimumInRow = currentRow[0];

            for (int j = 1; j <= secondString.length(); j++)
            {
                // Cost is 0 if characters match, 1 if substitution needed
                int substitutionCost = firstString.charAt(i - 1) == secondString.charAt(j - 1) ? 0 : 1;

                // Take minimum of three operations:
                currentRow[j] = Math.min(
                        Math.min(
                                previousRow[j] + 1,           // deletion
                                currentRow[j - 1] + 1         // insertion
                        ), previousRow[j - 1] + substitutionCost  // substitution
                );

                minimumInRow = Math.min(minimumInRow, currentRow[j]);
            }

            // Early termination: if minimum value in current row exceeds threshold, abort
            if (minimumInRow > maximumDistance)
            {
                return Integer.MAX_VALUE;
            }

            // Swap rows for next iteration
            int[] temp = previousRow;
            previousRow = currentRow;
            currentRow = temp;
        }

        return previousRow[secondString.length()];
    }

    /* CANDIDATE SELECTION */

    /**
     * Finds candidate items that might match the search query using the indices.
     * <p>
     * This method uses three strategies to find candidates:
     * <p>
     * 1. Exact word matches: Items containing the exact query as a word Example: query "diamond" finds items with
     * "diamond" in their name/path
     * <p>
     * 2. Prefix matches: Items containing words that start with the query Example: query "dia" finds items with
     * "diamond", "diagonal", etc. Uses the prefix index for O(#words_with_prefix) performance instead of O(#all_words)
     * <p>
     * 3. Trigram matches: Items sharing enough trigrams with the query Example: query "dimond" finds items with
     * "diamond" (fuzzy match)
     * <p>
     * This pre-filtering step dramatically improves performance by reducing the number of items that need detailed
     * scoring.
     *
     * @param normalizedQuery The search query in lowercase
     *
     * @return A set of item indices that are potential matches
     */
    private Set<Integer> findCandidateItemIndices(String normalizedQuery)
    {
        Set<Integer> candidateIndices = new HashSet<>();

        // Strategy 1: Check for exact word matches in the inverted index
        if (wordToItemIndicesMap.containsKey(normalizedQuery))
        {
            candidateIndices.addAll(wordToItemIndicesMap.get(normalizedQuery));
        }

        // Strategy 2: Check for prefix matches using the prefix index
        // This is MUCH faster than scanning all words in the inverted index
        String[] queryWords = normalizedQuery.split("[_\\-\\s/:\\\\]+");

        for (String queryWord : queryWords)
        {
            if (!queryWord.isEmpty())
            {
                // Use prefix index to find matching words efficiently
                int prefixLength = Math.min(queryWord.length(), MAXIMUM_PREFIX_LENGTH);
                String prefix = queryWord.substring(0, prefixLength);

                Set<String> matchingWords = prefixToWordsMap.getOrDefault(prefix, Collections.emptySet());

                for (String word : matchingWords)
                {
                    // Verify the word actually starts with the full query word
                    // (prefix index only stores up to 3 chars, query might be longer)
                    if (word.startsWith(queryWord))
                    {
                        List<Integer> itemIndices = wordToItemIndicesMap.get(word);
                        if (itemIndices != null)
                        {
                            candidateIndices.addAll(itemIndices);
                        }
                    }
                }
            }
        }

        // Strategy 3: Add fuzzy matches using the trigram index
        Set<String> queryTrigrams = generateTrigramsFromText(normalizedQuery);
        if (!queryTrigrams.isEmpty())
        {
            // Count how many query trigrams each item contains
            Map<Integer, Integer> itemTrigramCounts = new HashMap<>();

            for (String trigram : queryTrigrams)
            {
                Set<Integer> matchingItems = trigramToItemIndicesMap.getOrDefault(trigram, Collections.emptySet());

                for (Integer itemIndex : matchingItems)
                {
                    itemTrigramCounts.merge(itemIndex, 1, Integer::sum);
                }
            }

            // Add items that share at least 1/3 of the query's trigrams
            // This threshold balances between finding fuzzy matches and avoiding too many false positives
            int minimumTrigramOverlap = Math.max(1, queryTrigrams.size() / 3);
            itemTrigramCounts.forEach((itemIndex, trigramCount) -> {
                if (trigramCount >= minimumTrigramOverlap)
                {
                    candidateIndices.add(itemIndex);
                }
            });
        }

        return candidateIndices;
    }

    /* SCORING */

    /**
     * Calculates a comprehensive relevance score for how well an item matches the query.
     * <p>
     * LOWER SCORES ARE BETTER (0 = perfect match, 1000 = no match)
     * <p>
     * Scoring priority tiers (from best to worst):
     * <p>
     * EXACT MATCHES (scores 0-3): 0  - Display name exactly matches query 1  - Identifier/path exactly matches query 2
     * - A complete word in display name exactly matches query 3  - A complete word in identifier exactly matches query
     * <p>
     * PREFIX MATCHES (scores 5-30): 5  - Display name starts with query 10 - A word in display name starts with query
     * 15 - A word in identifier starts with query 20 - Display name contains query 25 - Identifier starts with query 30
     * - Identifier contains query
     * <p>
     * FUZZY MATCHES (scores 40-90): 40-55 - Levenshtein distance match in display name (closer = better) 50-65 -
     * Levenshtein distance match in identifier (closer = better) 60-80 - Trigram similarity match in display name 70-90
     * - Trigram similarity match in identifier
     * <p>
     * SUBSEQUENCE MATCHES (scores 100-110): 100 - Query is a subsequence of display name (e.g., "dmnd" in "diamond")
     * 110 - Query is a subsequence of identifier
     * <p>
     * NO MATCH: 1000 - Item doesn't match query in any meaningful way
     *
     * @param item            The item to score
     * @param normalizedQuery The search query in lowercase
     *
     * @return Scoring result containing the total score and match type
     */
    private ItemScoreResult calculateItemScore(T item, String normalizedQuery)
    {
        String displayName = SearchHandler.normalizeString(item.getPrimaryQuery());
        String identifierPath = SearchHandler.normalizeString(item.getSecondaryQuery());
        String fullIdentifier = identifierPath;

        // Remove namespace prefix from identifier if present (e.g., "minecraft:diamond" → "diamond")
        int namespaceColonIndex = fullIdentifier.indexOf(':');
        if (namespaceColonIndex >= 0 && namespaceColonIndex + 1 < fullIdentifier.length())
        {
            fullIdentifier = fullIdentifier.substring(namespaceColonIndex + 1);
        }

        int bestScore = 1000; // Default: no match
        ResultMatchType bestMatchType = ResultMatchType.NO_MATCH;

        /* === EXACT MATCHES (Highest Priority) === */

        // Perfect display name match (e.g., query "diamond sword" exactly matches item name)
        if (displayName.equals(normalizedQuery))
        {
            return new ItemScoreResult(0, ResultMatchType.EXACT_DISPLAY_NAME);
        }

        // Perfect identifier match (e.g., query "diamond_sword" exactly matches item path)
        if (fullIdentifier.equals(normalizedQuery) || identifierPath.equals(normalizedQuery))
        {
            return new ItemScoreResult(1, ResultMatchType.EXACT_IDENTIFIER);
        }

        /* === DISPLAY NAME MATCHING (High Priority) === */

        // Display name starts with query (e.g., "dia" matches "diamond")
        if (displayName.startsWith(normalizedQuery))
        {
            bestScore = 5;
            bestMatchType = ResultMatchType.PREFIX_MATCH;
        }

        // Split display name into individual words
        String[] displayNameWords = displayName.split("[_\\-\\s]+");

        // Exact word match in display name (e.g., "sword" matches "Diamond Sword")
        for (String word : displayNameWords)
        {
            if (word.equals(normalizedQuery))
            {
                return new ItemScoreResult(2, ResultMatchType.EXACT_DISPLAY_WORD);
            }
        }

        // Any word in display name starts with query (e.g., "dia" matches "Diamond Sword")
        for (String word : displayNameWords)
        {
            if (word.startsWith(normalizedQuery))
            {
                if (10 < bestScore)
                {
                    bestScore = 10;
                    bestMatchType = ResultMatchType.PREFIX_MATCH;
                }
                break;
            }
        }

        // Display name contains query anywhere (e.g., "mond" matches "Diamond Sword")
        if (displayName.contains(normalizedQuery))
        {
            if (20 < bestScore)
            {
                bestScore = 20;
                bestMatchType = ResultMatchType.SUBSTRING_MATCH;
            }
        }

        /* === IDENTIFIER MATCHING (Medium Priority) === */

        // Split identifier into words (e.g., "diamond_sword" → ["diamond", "sword"])
        String[] identifierWords = fullIdentifier.split("[_/:\\\\]+");

        // Exact word match in identifier
        for (String word : identifierWords)
        {
            if (word.equals(normalizedQuery))
            {
                return new ItemScoreResult(3, ResultMatchType.EXACT_IDENTIFIER_WORD);
            }
        }

        // Any identifier word starts with query
        for (String word : identifierWords)
        {
            if (word.startsWith(normalizedQuery))
            {
                if (15 < bestScore)
                {
                    bestScore = 15;
                    bestMatchType = ResultMatchType.PREFIX_MATCH;
                }
                break;
            }
        }

        // Identifier starts with query
        if (fullIdentifier.startsWith(normalizedQuery))
        {
            if (25 < bestScore)
            {
                bestScore = 25;
                bestMatchType = ResultMatchType.PREFIX_MATCH;
            }
        }

        // Identifier contains query
        if (fullIdentifier.contains(normalizedQuery))
        {
            if (30 < bestScore)
            {
                bestScore = 30;
                bestMatchType = ResultMatchType.SUBSTRING_MATCH;
            }
        }

        /* === FUZZY MATCHING (Lower Priority) === */

        // Levenshtein distance matching for short queries to handle typos
        // Only check if query is at least 3 characters (too short is unreliable)
        if (normalizedQuery.length() >= 3)
        {
            // Check against each word in display name
            for (String word : displayNameWords)
            {
                if (word.length() >= 3)
                {
                    int editDistance = calculateLevenshteinDistance(normalizedQuery, word, MAXIMUM_FUZZY_EDIT_DISTANCE);

                    if (editDistance <= MAXIMUM_FUZZY_EDIT_DISTANCE)
                    {
                        // Score: 40 + (edit distance × 5)
                        // Examples: distance 1 = score 45, distance 2 = score 50, distance 3 = score 55
                        int score = 40 + editDistance * 5;
                        if (score < bestScore)
                        {
                            bestScore = score;
                            bestMatchType = ResultMatchType.FUZZY_MATCH;
                        }
                    }
                }
            }

            // Check against each word in identifier
            for (String word : identifierWords)
            {
                if (word.length() >= 3)
                {
                    int editDistance = calculateLevenshteinDistance(normalizedQuery, word, MAXIMUM_FUZZY_EDIT_DISTANCE);

                    if (editDistance <= MAXIMUM_FUZZY_EDIT_DISTANCE)
                    {
                        // Score: 50 + (edit distance × 5)
                        int score = 50 + editDistance * 5;
                        if (score < bestScore)
                        {
                            bestScore = score;
                            bestMatchType = ResultMatchType.FUZZY_MATCH;
                        }
                    }
                }
            }
        }

        // Trigram similarity for longer queries (handles partial matches)
        // Only check if query is at least 4 characters (too short produces unreliable trigrams)
        if (normalizedQuery.length() >= 4)
        {
            double displayNameSimilarity = calculateTrigramSimilarity(normalizedQuery, displayName);
            if (displayNameSimilarity >= MINIMUM_TRIGRAM_SIMILARITY_THRESHOLD)
            {
                // Score: 60 + (dissimilarity × 20)
                // Examples: similarity 0.8 = score 64, similarity 0.5 = score 70, similarity 0.3 = score 74
                int score = 60 + (int) ((1.0 - displayNameSimilarity) * 20);
                if (score < bestScore)
                {
                    bestScore = score;
                    bestMatchType = ResultMatchType.FUZZY_MATCH;
                }
            }

            double identifierSimilarity = calculateTrigramSimilarity(normalizedQuery, fullIdentifier);
            if (identifierSimilarity >= MINIMUM_TRIGRAM_SIMILARITY_THRESHOLD)
            {
                // Score: 70 + (dissimilarity × 20)
                int score = 70 + (int) ((1.0 - identifierSimilarity) * 20);
                if (score < bestScore)
                {
                    bestScore = score;
                    bestMatchType = ResultMatchType.FUZZY_MATCH;
                }
            }
        }

        /* === SUBSEQUENCE MATCHING (Lowest Priority) === */

        // Check if query characters appear in order within the string
        // Example: "dmnd" is a subsequence of "diamond" (d-i-a-m-o-n-d)
        if (isSubsequenceOf(normalizedQuery, displayName))
        {
            if (100 < bestScore)
            {
                bestScore = 100;
                bestMatchType = ResultMatchType.SUBSEQUENCE_MATCH;
            }
        }
        else if (isSubsequenceOf(normalizedQuery, fullIdentifier))
        {
            if (110 < bestScore)
            {
                bestScore = 110;
                bestMatchType = ResultMatchType.SUBSEQUENCE_MATCH;
            }
        }

        return new ItemScoreResult(bestScore, bestMatchType);
    }

    /**
     * Checks if the needle string is a subsequence of the haystack string.
     * <p>
     * A subsequence means all characters from needle appear in haystack in the same order, but not necessarily
     * consecutively.
     * <p>
     * Examples: isSubsequenceOf("ace", "abcde") → true (a-b-c-d-e contains a-c-e in order) isSubsequenceOf("aec",
     * "abcde") → false (e comes before c in haystack) isSubsequenceOf("dmnd", "diamond") → true (d-i-a-m-o-n-d contains
     * d-m-n-d in order)
     * <p>
     * This is useful for matching abbreviations or partial typing patterns.
     *
     * @param needle   The sequence to search for
     * @param haystack The string to search within
     *
     * @return true if needle is a subsequence of haystack, false otherwise
     */
    private boolean isSubsequenceOf(String needle, String haystack)
    {
        int needlePosition = 0;

        for (int haystackPosition = 0; haystackPosition < haystack.length() && needlePosition < needle.length(); haystackPosition++)
        {
            if (haystack.charAt(haystackPosition) == needle.charAt(needlePosition))
            {
                needlePosition++;
            }
        }

        // If we've matched all characters from needle, it's a subsequence
        return needlePosition == needle.length();
    }

    /* HELPER CLASSES */

    /**
     * Represents a search result with its associated score and original position.
     * <p>
     * This is an internal data structure used during the scoring and sorting phase. The originalIndex preserves the
     * item's position in the source list, which is used as a tiebreaker when scores are equal (to maintain stable
     * sorting).
     */
    private record SearchResultWithScore<T>(T item, int score, int originalIndex)
    {}

    /**
     * Represents the score assigned to an item along with the type of match that produced it.
     * <p>
     * The matchType indicates which matching strategy produced the best score, which is useful for debugging,
     * telemetry, and understanding search behavior.
     */
    private record ItemScoreResult(int totalScore, ResultMatchType matchType)
    {}

    /**
     * Enumeration of all possible match types that can produce a score.
     * <p>
     * Each type corresponds to a specific matching strategy used during scoring. The match type in ItemScoreResult
     * indicates which strategy produced the best score.
     */
    private enum ResultMatchType
    {
        /**
         * Display name exactly equals the query
         */
        EXACT_DISPLAY_NAME,

        /**
         * Identifier or path exactly equals the query
         */
        EXACT_IDENTIFIER,

        /**
         * A complete word in display name exactly equals the query
         */
        EXACT_DISPLAY_WORD,

        /**
         * A complete word in identifier exactly equals the query
         */
        EXACT_IDENTIFIER_WORD,

        /**
         * Display name or identifier starts with query, or a word starts with query
         */
        PREFIX_MATCH,

        /**
         * Display name or identifier contains query as a substring
         */
        SUBSTRING_MATCH,

        /**
         * Match found through Levenshtein distance or trigram similarity
         */
        FUZZY_MATCH,

        /**
         * Query characters appear in order within display name or identifier
         */
        SUBSEQUENCE_MATCH,

        /**
         * No meaningful match found
         */
        NO_MATCH
    }
}