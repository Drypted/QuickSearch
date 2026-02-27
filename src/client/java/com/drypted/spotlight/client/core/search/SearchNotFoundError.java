package com.drypted.spotlight.client.core.search;

import com.drypted.spotlight.client.gui.models.InputError;
import com.drypted.spotlight.client.gui.models.InputFeedbackType;

public class SearchNotFoundError implements InputError
{
    @Override
    public String getMessage()
    {
        return "No Item Found";
    }

    @Override
    public InputFeedbackType getSeverity()
    {
        return InputFeedbackType.WARNING;
    }
}
