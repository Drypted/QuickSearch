package com.drypted.spotlight.client.gui.utils;

public class Color
{
    private int color;

    public Color(int rgba)
    {
        this.color = rgba;
    }

    public Color(int r, int g, int b)
    {
        this.color = fromRGBA(r, g, b, 255).asInt();
    }

    public Color(int r, int g, int b, int a)
    {
        this.color = fromRGBA(r, g, b, a).asInt();
    }

    public static Color fromRGBA(int r, int g, int b, int a)
    {
        // 0xFF = last 2 bytes i.e 0xFF = 0x000000FF
        // return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return new Color(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public void applyAlpha(int alpha)
    {
        this.color = (this.color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public void makeOpaque()
    {
        this.color = this.color | 0xFF000000;
    }

    public int asInt()
    {
        return this.color;
    }

    public Color withAlpha(int alpha)
    {
        return fromRGBA(
                (this.color >> 16) & 0xFF,
                (this.color >> 8) & 0xFF,
                this.color & 0xFF,
                alpha
        );
    }

    public Color withHalfAlpha()
    {
        return this.withAlpha(128);
    }

    /* GETTERS */

    public int getRed()
    {
        return (this.color >> 16) & 0xFF;
    }

    public int getGreen()
    {
        return (this.color >> 8) & 0xFF;
    }

    public int getBlue()
    {
        return this.color & 0xFF;
    }

    public int getAlpha()
    {
        return (this.color >> 24) & 0xFF;
    }
}
