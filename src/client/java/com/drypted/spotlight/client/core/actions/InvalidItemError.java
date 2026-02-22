package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.gui.models.InputError;
import com.drypted.spotlight.client.gui.models.InputErrorSeverity;

public class InvalidItemError implements InputError
{
    @Override
    public String getMessage()
    {
        return "Invalid Item";
    }

    @Override
    public InputErrorSeverity getSeverity()
    {
        return InputErrorSeverity.ERROR;
    }
}
