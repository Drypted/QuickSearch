package com.drypted.spotlight.client.core.blueprints.search;

import com.drypted.spotlight.client.core.blueprints.gui.InputError;
import com.drypted.spotlight.client.core.blueprints.gui.InputFeedbackType;

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
