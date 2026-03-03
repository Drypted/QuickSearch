package com.drypted.spotlight.client.core.blueprints.feedback;

import com.drypted.spotlight.client.core.blueprints.ui.common.Color;

public interface InputError
{
    String getMessage();

    InputFeedbackType getSeverity();

    default Color getColor()
    {
        return getSeverity().getColor();
    }

    default boolean haltsExecution()
    {
        return getSeverity().haltsExecution();
    }

    default boolean isNotNone()
    {
        return getSeverity() != InputFeedbackType.NONE;
    }
}
