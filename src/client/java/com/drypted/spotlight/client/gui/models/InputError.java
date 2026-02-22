package com.drypted.spotlight.client.gui.models;

import com.drypted.spotlight.client.gui.utils.Color;

public interface InputError
{
    String getMessage();

    InputErrorSeverity getSeverity();

    default Color getColor()
    {
        return getSeverity().getColor();
    }

    default boolean isCritical()
    {
        return getSeverity().isCritical();
    }

    default boolean isNone()
    {
        return getSeverity() == InputErrorSeverity.NONE;
    }
}
