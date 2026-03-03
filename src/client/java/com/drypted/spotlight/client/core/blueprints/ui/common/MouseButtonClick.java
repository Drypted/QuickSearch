package com.drypted.spotlight.client.core.blueprints.ui.common;

import net.minecraft.client.input.MouseButtonEvent;

/// Was added in 1.21.1, because of non-existence of {@link MouseButtonClick}
/// <br>
/// Kept for backwards compatibility
public record MouseButtonClick(double x, double y, int button)
{
    public MouseButtonClick(double x, double y)
    {
        this(x, y, 0);
    }

    public static MouseButtonClick from(MouseButtonEvent event)
    {
        return new MouseButtonClick(event.x(), event.y(), event.button());
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
