package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.records.RoundedCorners;
import com.drypted.spotlight.client.utils.Color;
import net.minecraft.client.gui.GuiGraphics;

public final class RenderUtils
{
    public static void fillRectangle(GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY, RoundedCorners corners, int outlineThickness, boolean renderOutline, Color backgroundColor, Color outlineColor)
    {
        outlineThickness = Math.max(0, outlineThickness);

        // Main body; inset by outlineThickness
        g.fill(
                startPosX + outlineThickness,
                startPosY + outlineThickness,
                endPosX - outlineThickness,
                endPosY - outlineThickness,
                backgroundColor.asInt()
        );

        // Stripes; inset by outlineThickness
        // left
        g.fill(
                startPosX,
                startPosY + outlineThickness,
                startPosX + outlineThickness,
                endPosY - outlineThickness,
                outlineColor.asInt()
        );
        // right
        g.fill(
                endPosX - outlineThickness,
                startPosY + outlineThickness,
                endPosX,
                endPosY - outlineThickness,
                outlineColor.asInt()
        );
        // top
        g.fill(
                startPosX + outlineThickness,
                startPosY,
                endPosX - outlineThickness,
                startPosY + outlineThickness,
                outlineColor.asInt()
        );
        // bottom
        g.fill(
                startPosX + outlineThickness,
                endPosY - outlineThickness,
                endPosX - outlineThickness,
                endPosY,
                outlineColor.asInt()
        );

        if (!renderOutline || outlineThickness == 0)
        {
            return;
        }

        // Corner pixels
        if (corners.topLeft())
        {
            g.fill(
                    startPosX + outlineThickness,
                    startPosY + outlineThickness,
                    startPosX + (2 * outlineThickness),
                    startPosY + (2 * outlineThickness),
                    outlineColor.asInt()
            );
        }
        else
        {
            g.fill(startPosX, startPosY, startPosX + outlineThickness, startPosY + outlineThickness, backgroundColor.asInt());
        }

        if (corners.topRight())
        {
            g.fill(
                    endPosX - (2 * outlineThickness),
                    startPosY + outlineThickness,
                    endPosX - outlineThickness,
                    startPosY + (2 * outlineThickness),
                    outlineColor.asInt()
            );
        }
        else
        {
            g.fill(endPosX - outlineThickness, startPosY, endPosX, startPosY + outlineThickness, backgroundColor.asInt());
        }

        if (corners.bottomLeft())
        {
            g.fill(
                    startPosX + outlineThickness,
                    endPosY - (2 * outlineThickness),
                    startPosX + (2 * outlineThickness),
                    endPosY - outlineThickness,
                    outlineColor.asInt()
            );
        }
        else
        {
            g.fill(startPosX, endPosY - outlineThickness, startPosX + outlineThickness, endPosY, backgroundColor.asInt());
        }

        if (corners.bottomRight())
        {
            g.fill(
                    endPosX - (2 * outlineThickness),
                    endPosY - (2 * outlineThickness),
                    endPosX - outlineThickness,
                    endPosY - outlineThickness,
                    outlineColor.asInt()
            );
        }
        else
        {
            g.fill(endPosX - outlineThickness, endPosY - outlineThickness, endPosX, endPosY, backgroundColor.asInt());
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
