package com.drypted.spotlight.client.core.blueprints.commands.argument;

/**
 * Thrown when an argument cannot be parsed from the raw input.
 */
public class ArgumentParseException extends Exception
{
    public ArgumentParseException(String message)
    {
        super(message);
    }
}

