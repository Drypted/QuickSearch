package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.utils.Color;
import net.minecraft.client.gui.GuiGraphics;

public final class RenderUtils
{
    public static void fillRectangle(GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY, boolean isRounded, int outlineThickness, boolean renderOutline, Color backgroundColor, Color outlineColor)
    {
        outlineThickness = Math.max(0, outlineThickness);

        if (isRounded)
        {
            // Main body; inset by outline pixel to allow for outline
            g.fill(
                    startPosX + outlineThickness,
                    startPosY + outlineThickness,
                    endPosX - outlineThickness,
                    endPosY - outlineThickness,
                    backgroundColor.asInt()
            );

            // stripes
            g.fill(
                    startPosX,
                    startPosY + outlineThickness,
                    startPosX + outlineThickness,
                    endPosY - outlineThickness,
                    outlineColor.asInt()
            ); // left
            g.fill(
                    endPosX - outlineThickness,
                    startPosY + outlineThickness,
                    endPosX,
                    endPosY - outlineThickness,
                    outlineColor.asInt()
            ); // right
            g.fill(
                    startPosX + outlineThickness,
                    startPosY,
                    endPosX - outlineThickness,
                    startPosY + outlineThickness,
                    outlineColor.asInt()
            ); // top
            g.fill(
                    startPosX + outlineThickness,
                    endPosY - outlineThickness,
                    endPosX - outlineThickness,
                    endPosY,
                    outlineColor.asInt()
            ); // top

            // corner pixels
            if (renderOutline)
            {
                g.fill(
                        startPosX + outlineThickness,
                        startPosY + outlineThickness,
                        startPosX + (2 * outlineThickness),
                        startPosY + (2 * outlineThickness),
                        outlineColor.asInt()
                ); // top-left
                g.fill(
                        endPosX - (2 * outlineThickness),
                        startPosY + outlineThickness,
                        endPosX - outlineThickness,
                        startPosY + (2 * outlineThickness),
                        outlineColor.asInt()
                ); // top-right
                g.fill(
                        startPosX + outlineThickness,
                        endPosY - (2 * outlineThickness),
                        startPosX + (2 * outlineThickness),
                        endPosY - outlineThickness,
                        outlineColor.asInt()
                ); // bottom-left
                g.fill(
                        endPosX - (2 * outlineThickness),
                        endPosY - (2 * outlineThickness),
                        endPosX - outlineThickness,
                        endPosY - outlineThickness,
                        outlineColor.asInt()
                ); // bottom-right
            }
        }
        else
        {
            // Main body
            g.fill(startPosX, startPosY, endPosX, endPosY, backgroundColor.asInt());

            // outline
            if (renderOutline)
            {
                g.fill(
                        startPosX,
                        startPosY + outlineThickness,
                        startPosX + outlineThickness,
                        endPosY,
                        outlineColor.asInt()
                ); // left; see y only
                g.fill(
                        startPosX,
                        startPosY,
                        endPosX - outlineThickness,
                        startPosY + outlineThickness,
                        outlineColor.asInt()
                ); // top; see x only
                g.fill(
                        endPosX - outlineThickness,
                        startPosY,
                        endPosX,
                        endPosY - outlineThickness,
                        outlineColor.asInt()
                ); // right; see y only
                g.fill(
                        startPosX + outlineThickness,
                        endPosY - outlineThickness,
                        endPosX,
                        endPosY,
                        outlineColor.asInt()
                ); // bottom; see x only
            }
        }
    }

    public static void drawHorizontalLine(GuiGraphics g, int startPosX, int endPosX, int posY, int thickness, Color color)
    {
        g.fill(startPosX, posY, endPosX, posY + thickness, color.asInt());
    }

    public static void drawVerticalLine(GuiGraphics g, int posX, int startPosY, int endPosY, int thickness, Color color)
    {
        g.fill(posX, startPosY, posX + thickness, endPosY, color.asInt());
    }
}
