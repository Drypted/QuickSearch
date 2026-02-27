package com.drypted.spotlight.client.gui.models;

import com.drypted.spotlight.client.gui.utils.Color;

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

    default boolean isNone()
    {
        return getSeverity() == InputFeedbackType.NONE;
    }
}
