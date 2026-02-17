package com.drypted.spotlight.client.gui.models;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.styling.Styles;

public enum InputErrorSeverity
{
    NONE(true),
    WARNING(true),
    ERROR(false);

    private final boolean ignorable;

    InputErrorSeverity(boolean ignorable)
    {
        this.ignorable = ignorable;
    }

    public boolean isIgnorable()
    {
        return ignorable;
    }

    public Color getColor()
    {
        return switch (this)
        {
            case NONE -> Colors.CLEAR;
            case WARNING -> Styles.Input.WARNING_COLOR;
            case ERROR -> Styles.Input.ERROR_COLOR;
        };
    }

    public String getName()
    {
        return switch (this)
        {
            case NONE -> "None";
            case WARNING -> "Warning";
            case ERROR -> "Error";
        };
    }
}
