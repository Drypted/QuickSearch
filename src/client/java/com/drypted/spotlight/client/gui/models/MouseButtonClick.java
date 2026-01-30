package com.drypted.spotlight.client.gui.models;

public record MouseButtonClick(double x, double y, int button)
{
    public MouseButtonClick(double x, double y)
    {
        this(x, y, 0);
    }

    public static MouseButtonClick from(double x, double y)
    {
        return new MouseButtonClick(x, y, 0);
    }

    public static MouseButtonClick from(double x, double y, int button)
    {
        return new MouseButtonClick(x, y, button);
    }
}
