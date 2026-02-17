package com.drypted.spotlight.client.gui.models;

public enum ValidationSeverity
{
    NONE(true),
    WARNING(true),
    ERROR(false);

    private final boolean ignorable;

    ValidationSeverity(boolean ignorable)
    {
        this.ignorable = ignorable;
    }

    public boolean isIgnorable()
    {
        return ignorable;
    }
}
