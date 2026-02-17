package com.drypted.spotlight.client.core.commands;

import com.drypted.spotlight.client.gui.models.InputError;
import com.drypted.spotlight.client.gui.models.InputErrorSeverity;

public record CommandError(String error, InputErrorSeverity severity) implements InputError
{
    public boolean isNone()
    {
        return this.severity == InputErrorSeverity.NONE || this.equals(NONE);
    }

    public boolean isIgnorable()
    {
        return severity.isIgnorable();
    }

    /* VALIDATION ERROR IMPLEMENTATION */

    @Override
    public String getMessage()
    {
        return this.error;
    }

    @Override
    public InputErrorSeverity getSeverity()
    {
        return severity;
    }

    /* STATICS */

    public static final CommandError NONE = new CommandError("", InputErrorSeverity.NONE);

    public static CommandError withWarning(String error)
    {
        return new CommandError(error, InputErrorSeverity.WARNING);
    }

    public static CommandError withError(String error)
    {
        return new CommandError(error, InputErrorSeverity.ERROR);
    }
}
