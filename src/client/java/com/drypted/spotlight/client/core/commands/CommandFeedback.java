package com.drypted.spotlight.client.core.commands;

import com.drypted.spotlight.client.gui.models.InputError;
import com.drypted.spotlight.client.gui.models.InputErrorSeverity;

public record CommandFeedback(String message, InputErrorSeverity severity) implements InputError
{
    @Override
    public boolean isNone()
    {
        return this.severity == InputErrorSeverity.NONE || this.equals(NO_ERROR);
    }

    /* VALIDATION ERROR IMPLEMENTATION */

    @Override
    public String getMessage()
    {
        return this.message;
    }

    @Override
    public InputErrorSeverity getSeverity()
    {
        return severity;
    }

    /* STATICS */

    public static CommandFeedback withInfo(String message)
    {
        return new CommandFeedback(message, InputErrorSeverity.INFO);
    }


    public static CommandFeedback withWarning(String message)
    {
        return new CommandFeedback(message, InputErrorSeverity.WARNING);
    }

    public static CommandFeedback withError(String message)
    {
        return new CommandFeedback(message, InputErrorSeverity.ERROR);
    }

    public static CommandFeedback NO_ERROR = new CommandFeedback("", InputErrorSeverity.NONE);
}
