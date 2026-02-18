package com.drypted.spotlight.client.core.search;

import com.drypted.spotlight.client.gui.models.InputError;
import com.drypted.spotlight.client.gui.models.InputErrorSeverity;

public class SearchNotFoundError implements InputError
{
    @Override
    public String getMessage()
    {
        return "No Valid Item Found";
    }

    @Override
    public InputErrorSeverity getSeverity()
    {
        return InputErrorSeverity.WARNING;
    }
}
