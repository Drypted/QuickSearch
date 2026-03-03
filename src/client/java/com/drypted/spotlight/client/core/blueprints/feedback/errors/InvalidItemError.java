package com.drypted.spotlight.client.core.blueprints.feedback.errors;

import com.drypted.spotlight.client.core.blueprints.feedback.InputError;
import com.drypted.spotlight.client.core.blueprints.feedback.InputFeedbackType;
import com.drypted.spotlight.client.ui.components.InputWidget;

/// Feedback for {@link InputWidget}
public class InvalidItemError implements InputError
{
    @Override
    public String getMessage()
    {
        return "Invalid Item";
    }

    @Override
    public InputFeedbackType getSeverity()
    {
        return InputFeedbackType.ERROR;
    }
}
