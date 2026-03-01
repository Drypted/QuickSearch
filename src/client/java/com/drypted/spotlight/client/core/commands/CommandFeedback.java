package com.drypted.spotlight.client.core.commands;

import com.drypted.spotlight.client.gui.models.InputError;
import com.drypted.spotlight.client.gui.models.InputFeedbackType;

public record CommandFeedback(String message, InputFeedbackType severity) implements InputError
{
    @Override
    public boolean isNotNone()
    {
        return this.severity != InputFeedbackType.NONE && !this.equals(NO_ERROR);
    }

    /* VALIDATION ERROR IMPLEMENTATION */

    @Override
    public String getMessage()
    {
        return this.message;
    }

    @Override
    public InputFeedbackType getSeverity()
    {
        return severity;
    }

    /* STATICS */

    public static CommandFeedback withInfo(String message)
    {
        return new CommandFeedback(message, InputFeedbackType.INFO);
    }

    public static CommandFeedback withSuccess(String message)
    {
        return new CommandFeedback(message, InputFeedbackType.SUCCESS);
    }

    public static CommandFeedback withWarning(String message)
    {
        return new CommandFeedback(message, InputFeedbackType.WARNING);
    }

    public static CommandFeedback withError(String message)
    {
        return new CommandFeedback(message, InputFeedbackType.ERROR);
    }

    public static CommandFeedback NO_ERROR = new CommandFeedback("", InputFeedbackType.NONE);
}
