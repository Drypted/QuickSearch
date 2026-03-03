package com.drypted.spotlight.client.core.blueprints.ui.common;

public record RoundedCorners(boolean topLeft, boolean topRight, boolean bottomLeft,
                             boolean bottomRight)
{
    public static RoundedCorners from(boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight)
    {
        return new RoundedCorners(topLeft, topRight, bottomLeft, bottomRight);
    }

    public static RoundedCorners fromSingle(boolean all)
    {
        return RoundedCorners.from(all, all, all, all);
    }

    public static RoundedCorners fromVerticalSides(boolean top, boolean bottom)
    {
        return RoundedCorners.from(top, top, bottom, bottom);
    }

    public static RoundedCorners fromHorizontalSides(boolean left, boolean right)
    {
        return RoundedCorners.from(left, right, left, right);
    }

    public static RoundedCorners all()
    {
        return RoundedCorners.from(true, true, true, true);
    }

    public static RoundedCorners none()
    {
        return RoundedCorners.from(false, false, false, false);
    }

    public boolean isNone()
    {
        return !topLeft && !topRight && !bottomLeft && !bottomRight;
    }

    public boolean isAll()
    {
        return topLeft && topRight && bottomLeft && bottomRight;
    }
}