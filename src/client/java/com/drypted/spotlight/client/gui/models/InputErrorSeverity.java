package com.drypted.spotlight.client.gui.models;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.styling.Styles;

public enum InputErrorSeverity
{
    NONE(false),
    WARNING(false),
    ERROR(true);

    private final boolean critical;

    InputErrorSeverity(boolean critical)
    {
        this.critical = critical;
    }

    /// Critical Errors should halt execution
    public boolean isCritical()
    {
        return critical;
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
