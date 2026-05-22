package com.drypted.quicksearch.client.core.blueprints.ui.common;

public record OutlinedSides(boolean left, boolean top, boolean right, boolean bottom)
{
    public static OutlinedSides from(boolean left, boolean top, boolean right, boolean bottom)
    { return new OutlinedSides(left, top, right, bottom); }

    public static OutlinedSides fromSingle(boolean all) { return OutlinedSides.from(all, all, all, all); }

    public static OutlinedSides fromVerticalSides(boolean top, boolean bottom)
    {
        return OutlinedSides.from(
                true,
                top,
                true,
                bottom
        );
    }

    public static OutlinedSides fromHorizontalSides(boolean left, boolean right)
    {
        return OutlinedSides.from(
                left,
                true,
                right,
                true
        );
    }

    public static OutlinedSides all() { return OutlinedSides.from(true, true, true, true); }

    public static OutlinedSides none() { return OutlinedSides.from(false, false, false, false); }

    public boolean isNone() { return !left && !top && !right && !bottom; }

    public boolean isAll() { return left && top && right && bottom; }
}
