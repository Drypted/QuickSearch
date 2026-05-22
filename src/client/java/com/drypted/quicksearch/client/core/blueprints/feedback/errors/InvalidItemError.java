package com.drypted.quicksearch.client.core.blueprints.feedback.errors;

import com.drypted.quicksearch.client.core.blueprints.feedback.InputError;
import com.drypted.quicksearch.client.core.blueprints.feedback.InputFeedbackType;
import com.drypted.quicksearch.client.ui.components.InputWidget;

/// Feedback for {@link InputWidget}
public class InvalidItemError implements InputError
{
    @Override
    public String getMessage() { return "Invalid Item"; }

    @Override
    public InputFeedbackType getSeverity() { return InputFeedbackType.ERROR; }
}
