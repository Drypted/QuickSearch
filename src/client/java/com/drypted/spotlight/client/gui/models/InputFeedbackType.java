package com.drypted.spotlight.client.gui.models;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import com.drypted.spotlight.client.styling.Styles;
import net.minecraft.ChatFormatting;

public enum InputFeedbackType
{
    NONE(false),
    INFO(false),
    SUCCESS(false),
    WARNING(false),
    ERROR(true);

    private final boolean haltsExecution;

    InputFeedbackType(boolean haltsExecution)
    {
        this.haltsExecution = haltsExecution;
    }

    /// Critical Errors should halt execution
    public boolean haltsExecution()
    {
        return haltsExecution;
    }

    public Color getColor()
    {
        return switch (this)
        {
            case NONE -> Colors.CLEAR;
            case INFO -> Styles.Input.INFO_COLOR;
            case SUCCESS -> Styles.Input.SUCCESS_COLOR;
            case WARNING -> Styles.Input.WARNING_COLOR;
            case ERROR -> Styles.Input.ERROR_COLOR;
        };
    }

    public ChatFormatting getChatColor()
    {
        return switch (this)
        {
            case NONE -> ChatFormatting.RESET;
            case INFO -> ChatFormatting.AQUA;
            case SUCCESS -> ChatFormatting.GREEN;
            case WARNING -> ChatFormatting.GOLD;
            case ERROR -> ChatFormatting.RED;
        };
    }

    public String getName()
    {
        return switch (this)
        {
            case NONE -> "None";
            case INFO -> "Info";
            case SUCCESS -> "Success";
            case WARNING -> "Warning";
            case ERROR -> "Error";
        };
    }
}
