package com.drypted.spotlight.client.core.algorithms;

import com.drypted.spotlight.client.models.SearchResultData;

import java.util.*;
import java.util.stream.Stream;

public class SmartSearch
{
    private static final int FUZZY_THRESHOLD = 3; // Maximum edit distance for fuzzy matching
    private static final double TRIGRAM_THRESHOLD = 0.3; // Minimum similarity score (0-1)

    // Items managed by this instance
    private List<SearchResultData> items = Collections.emptyList();

    // Inverted index: word -> list of item indices
    private Map<String, List<Integer>> InvertedIndex = Collections.emptyMap();

    // Trigram index for fuzzy matching
    private Map<String, Set<Integer>> TrigramIndex = Collections.emptyMap();

    /**
     * Construct a SmartSearch instance and build indices for the provided items.
     */
    public SmartSearch(List<SearchResultData> items)
    {
        if (items != null)
        {
            this.items = new ArrayList<>(items);
        }

        rebuildIndices(this.items);
    }

    /**
     * Rebuild indices for a new list of items. Replaces the current item list.
     */
    public void rebuildIndices(List<SearchResultData> items)
    {
        if (items == null)
        {
            this.items = Collections.emptyList();
        }
        else
        {
            this.items = new ArrayList<>(items);
        }
        buildInvertedIndex();
        buildTrigramIndex();
    }

    public Stream<SearchResultData> search(String query, int maxResults)
    {
        String q = query.toLowerCase();

        Set<Integer> candidates = getCandidatesFromIndex(q);

        if (candidates.isEmpty() || candidates.size() < 10)
        {
            candidates = new HashSet<>();
            for (int i = 0; i < items.size(); i++)
            {
                candidates.add(i);
            }
        }

        return candidates.stream() //
                         .map(i -> {
                             SearchResultData item = items.get(i);
                             ScoringResult score = scoreItem(item, q);

                             if (score.totalScore() >= 1000)
                             {
                                 return null;
                             }

                             return new SearchResult(item, score.totalScore(), i);
                         }).filter(Objects::nonNull)
                         .sorted(Comparator.comparingInt(SearchResult::score)
                                           .thenComparingInt(SearchResult::originalIndex)).limit(
                        maxResults > 0 ? maxResults : Integer.MAX_VALUE).map(SearchResult::item);
    }

    /**
     * Builds an inverted index mapping words to item indices for faster searching.
     */
    private void buildInvertedIndex()
    {
        Map<String, List<Integer>> index = new HashMap<>();

        for (int i = 0; i < items.size(); i++)
        {
            SearchResultData item = items.get(i);

            // Index the identifier path
            String path = item.getIdentifier().getPath().toLowerCase();
            String[] pathWords = path.split("[_\\-\\s]+");

            for (String word : pathWords)
            {
                if (!word.isEmpty())
                {
                    index.computeIfAbsent(word, k -> new ArrayList<>()).add(i);
                }
            }

            // Index the display name
            String displayName = item.getName().toLowerCase();
            String[] nameWords = displayName.split("[_\\-\\s]+");

            for (String word : nameWords)
            {
                if (!word.isEmpty())
                {
                    index.computeIfAbsent(word, k -> new ArrayList<>()).add(i);
                }
            }

            // Also index the full strings
            index.computeIfAbsent(path, k -> new ArrayList<>()).add(i);
            index.computeIfAbsent(displayName, k -> new ArrayList<>()).add(i);
        }

        InvertedIndex = Collections.unmodifiableMap(index);
    }

    /**
     * Builds a trigram index for fuzzy matching support.
     */
    private void buildTrigramIndex()
    {
        Map<String, Set<Integer>> index = new HashMap<>();

        for (int i = 0; i < items.size(); i++)
        {
            SearchResultData item = items.get(i);

            // Generate trigrams from identifier path
            String path = item.getIdentifier().getPath().toLowerCase();
            Set<String> pathTrigrams = generateTrigrams(path);

            for (String trigram : pathTrigrams)
            {
                index.computeIfAbsent(trigram, k -> new HashSet<>()).add(i);
            }

            // Generate trigrams from display name
            String displayName = item.getName().toLowerCase();
            Set<String> nameTrigrams = generateTrigrams(displayName);

            for (String trigram : nameTrigrams)
            {
                index.computeIfAbsent(trigram, k -> new HashSet<>()).add(i);
            }
        }

        TrigramIndex = Collections.unmodifiableMap(index);
    }

    /**
     * Generates trigrams from a string for fuzzy matching.
     */
    private Set<String> generateTrigrams(String text)
    {
        Set<String> trigrams = new HashSet<>();

        if (text.length() < 3)
        {
            trigrams.add(text);
            return trigrams;
        }

        // Add padding for beginning and end
        String padded = "  " + text + "  ";

        for (int i = 0; i < padded.length() - 2; i++)
        {
            trigrams.add(padded.substring(i, i + 3));
        }

        return trigrams;
    }

    /**
     * Calculates trigram similarity between two strings (0-1, higher is more similar).
     */
    private double calculateTrigramSimilarity(String s1, String s2)
    {
        Set<String> trigrams1 = generateTrigrams(s1);
        Set<String> trigrams2 = generateTrigrams(s2);

        if (trigrams1.isEmpty() && trigrams2.isEmpty())
            return 1.0;

        Set<String> intersection = new HashSet<>(trigrams1);
        intersection.retainAll(trigrams2);

        Set<String> union = new HashSet<>(trigrams1);
        union.addAll(trigrams2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Calculates Levenshtein distance between two strings.
     */
    private int levenshteinDistance(String s1, String s2)
    {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++)
        {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++)
        {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++)
        {
            for (int j = 1; j <= s2.length(); j++)
            {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Gets candidate item indices from the inverted index.
     */
    private Set<Integer> getCandidatesFromIndex(String query)
    {
        Set<Integer> candidates = new HashSet<>();

        // Check for exact word matches
        if (InvertedIndex.containsKey(query))
        {
            candidates.addAll(InvertedIndex.get(query));
        }

        // Check for prefix matches
        String[] queryWords = query.split("[_\\-\\s]+");
        for (String word : queryWords)
        {
            if (!word.isEmpty())
            {
                InvertedIndex.forEach((indexedWord, indices) -> {
                    if (indexedWord.startsWith(word))
                    {
                        candidates.addAll(indices);
                    }
                });
            }
        }

        // Add fuzzy matches using trigram index
        Set<String> queryTrigrams = generateTrigrams(query);
        if (!queryTrigrams.isEmpty())
        {
            Map<Integer, Integer> trigramCounts = new HashMap<>();

            for (String trigram : queryTrigrams)
            {
                Set<Integer> matches = TrigramIndex.getOrDefault(trigram, Collections.emptySet());
                for (Integer idx : matches)
                {
                    trigramCounts.merge(idx, 1, Integer::sum);
                }
            }

            // Add items with significant trigram overlap
            int threshold = Math.max(1, queryTrigrams.size() / 3);
            trigramCounts.forEach((idx, count) -> {
                if (count >= threshold)
                {
                    candidates.add(idx);
                }
            });
        }

        return candidates;
    }

    /**
     * Comprehensive scoring for search results.
     * Lower scores are better (0 = perfect match).
     */
    private ScoringResult scoreItem(SearchResultData item, String query)
    {
        String displayName = item.getName().toLowerCase();
        String identifier = item.getIdentifier().toString().toLowerCase();
        String path = item.getIdentifier().getPath().toLowerCase();

        // Remove namespace from identifier if present
        int colonIdx = identifier.indexOf(':');
        if (colonIdx >= 0 && colonIdx + 1 < identifier.length())
        {
            identifier = identifier.substring(colonIdx + 1);
        }

        int score = 1000; // Default: no match

        // === EXACT MATCHES (Highest Priority) ===

        // Perfect display name match
        if (displayName.equals(query))
        {
            return new ScoringResult(0, "exact_display_name");
        }

        // Perfect identifier match
        if (identifier.equals(query) || path.equals(query))
        {
            return new ScoringResult(1, "exact_identifier");
        }

        // === DISPLAY NAME MATCHING (High Priority) ===

        // Display name starts with query
        if (displayName.startsWith(query))
        {
            score = Math.min(score, 5);
        }

        // Display name word starts with query
        String[] displayWords = displayName.split("[_\\-\\s]+");
        for (String word : displayWords)
        {
            if (word.startsWith(query))
            {
                score = Math.min(score, 10);
                break;
            }
        }

        // Display name contains query
        if (displayName.contains(query))
        {
            score = Math.min(score, 20);
        }

        // === IDENTIFIER MATCHING (Medium Priority) ===

        // Split identifier into words
        String[] identifierParts = identifier.split("_");

        // Any word starts with query
        boolean anyWordStarts = false;
        for (String part : identifierParts)
        {
            if (part.startsWith(query))
            {
                anyWordStarts = true;
                break;
            }
        }

        if (anyWordStarts)
        {
            score = Math.min(score, 15);
        }

        // Identifier starts with query
        if (identifier.startsWith(query))
        {
            score = Math.min(score, 25);
        }

        // Identifier contains query
        if (identifier.contains(query))
        {
            score = Math.min(score, 30);
        }

        // === FUZZY MATCHING (Lower Priority) ===

        // Levenshtein distance for short queries (typo tolerance)
        if (query.length() >= 3)
        {
            // Check against display name words
            for (String word : displayWords)
            {
                if (word.length() >= 3)
                {
                    int distance = levenshteinDistance(query, word);
                    if (distance <= FUZZY_THRESHOLD)
                    {
                        score = Math.min(score, 40 + distance * 5);
                    }
                }
            }

            // Check against identifier parts
            for (String part : identifierParts)
            {
                if (part.length() >= 3)
                {
                    int distance = levenshteinDistance(query, part);
                    if (distance <= FUZZY_THRESHOLD)
                    {
                        score = Math.min(score, 50 + distance * 5);
                    }
                }
            }
        }

        // Trigram similarity for longer queries
        if (query.length() >= 4)
        {
            double displaySimilarity = calculateTrigramSimilarity(query, displayName);
            if (displaySimilarity >= TRIGRAM_THRESHOLD)
            {
                score = Math.min(score, 60 + (int) ((1.0 - displaySimilarity) * 20));
            }

            double identifierSimilarity = calculateTrigramSimilarity(query, identifier);
            if (identifierSimilarity >= TRIGRAM_THRESHOLD)
            {
                score = Math.min(score, 70 + (int) ((1.0 - identifierSimilarity) * 20));
            }
        }

        // === SUBSEQUENCE MATCHING (Lowest Priority) ===

        // Check if query is a subsequence of display name or identifier
        if (isSubsequence(query, displayName))
        {
            score = Math.min(score, 100);
        }
        else if (isSubsequence(query, identifier))
        {
            score = Math.min(score, 110);
        }

        return new ScoringResult(score, "default");
    }

    /**
     * Checks if needle is a subsequence of haystack.
     */
    private boolean isSubsequence(String needle, String haystack)
    {
        int needleIdx = 0;

        for (int i = 0; i < haystack.length() && needleIdx < needle.length(); i++)
        {
            if (haystack.charAt(i) == needle.charAt(needleIdx))
            {
                needleIdx++;
            }
        }

        return needleIdx == needle.length();
    }


    /* Helper Classes */

    private record SearchResult(SearchResultData item, int score, int originalIndex)
    { }

    private record ScoringResult(int totalScore, String matchType)
    { }
}
