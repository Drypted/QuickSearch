package com.drypted.quicksearch.client.core.blueprints.feedback.errors;

import com.drypted.quicksearch.client.core.blueprints.feedback.InputError;
import com.drypted.quicksearch.client.core.blueprints.feedback.InputFeedbackType;

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
