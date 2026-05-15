package com.drypted.quicksearch.client.core.blueprints.commands.argument;

import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;

import java.util.List;

/**
 * Defines a typed command argument with parsing, validation, and suggestion capabilities.
 *
 * @param <T> The parsed result type
 */
public interface ArgumentType<T>
{
    /**
     * Parse the raw string into the typed value.
     *
     * @param raw The raw user input for this argument slot
     *
     * @return The parsed value
     *
     * @throws ArgumentParseException if parsing fails
     */
    T parse(String raw) throws ArgumentParseException;

    /**
     * Validate the raw string without fully parsing it.
     *
     * @param raw The raw user input for this argument slot
     *
     * @return {@link CommandFeedback#NO_ERROR} if valid, or an error/warning feedback
     */
    CommandFeedback validate(String raw);

    /**
     * Provide suggestions for the current partial input.
     *
     * @param partial The partial text the user has typed so far for this argument (may be empty)
     *
     * @return A list of suggestion strings, may be empty
     */
    List<String> getSuggestions(String partial);

    /**
     * A short usage hint shown in the command signature, e.g. {@code <username>} or {@code <name>}.
     */
    String getUsageHint();
}

