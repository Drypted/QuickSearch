package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.core.blueprints.gui.InputError;
import com.drypted.spotlight.client.core.blueprints.gui.InputFeedbackType;

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
