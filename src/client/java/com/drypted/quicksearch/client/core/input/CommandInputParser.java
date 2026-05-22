package com.drypted.quicksearch.client.core.input;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandInputParser
{
    private static final Pattern ARG_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s\"]+)");

    private CommandInputParser() { }

    public static boolean isCommandInput(String text) { return text != null && text.startsWith("/"); }

    public static boolean hasStartedArguments(String text)
    {
        if (!isCommandInput(text)) return false;

        String afterSlash = text.substring(1);
        if (afterSlash.isEmpty()) return false;

        String commandName = afterSlash.split("\\s+")[0];
        return afterSlash.length() > commandName.length();
    }

    public static String getCommandName(String text)
    {
        if (!isCommandInput(text)) return "";

        String trimmed = text.trim();
        if (trimmed.length() <= 1) return "";

        String afterSlash = trimmed.substring(1);
        int spaceIndex = afterSlash.indexOf(' ');

        if (spaceIndex == -1) return afterSlash;
        return afterSlash.substring(0, spaceIndex);
    }

    public static String @NotNull [] getArgs(String text)
    {
        if (text == null) return new String[0];

        String trimmed = text.trim();
        int spaceIndex = trimmed.indexOf(' ');

        // No space means there are no arguments.
        if (spaceIndex == -1) return new String[0];

        String args = trimmed.substring(spaceIndex + 1).trim();
        if (args.isEmpty()) return new String[0];

        List<String> matches = new ArrayList<>();
        Matcher matcher = ARG_PATTERN.matcher(args);

        while (matcher.find())
        {
            if (matcher.group(1) != null)
            {
                matches.add(matcher.group(1));
            }
            else if (matcher.group(2) != null)
            {
                matches.add(matcher.group(2));
            }
        }

        return matches.toArray(new String[0]);
    }
}