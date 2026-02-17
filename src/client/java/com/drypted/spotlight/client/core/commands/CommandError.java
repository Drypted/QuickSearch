package com.drypted.spotlight.client.core.commands;

import com.drypted.spotlight.client.gui.models.ValidationError;
import com.drypted.spotlight.client.gui.models.ValidationSeverity;

public record CommandError(String error, ValidationSeverity severity) implements ValidationError
{
    public boolean isNone()
    {
        return this.severity == ValidationSeverity.NONE || this.equals(NONE);
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
    public ValidationSeverity getSeverity()
    {
        return severity;
    }

    /* STATICS */

    public static final CommandError NONE = new CommandError("", ValidationSeverity.NONE);

    public static CommandError withWarning(String error)
    {
        return new CommandError(error, ValidationSeverity.WARNING);
    }

    public static CommandError withError(String error)
    {
        return new CommandError(error, ValidationSeverity.ERROR);
    }
}
