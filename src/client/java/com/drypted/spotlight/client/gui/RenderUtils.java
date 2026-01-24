package com.drypted.spotlight.client.gui;

import com.drypted.spotlight.client.records.RoundedCorners;
import com.drypted.spotlight.client.utils.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class RenderUtils
{
    private static final int VANILLA_ITEM_SIZE = 16;

    /**
     * Fills a rectangle with optional rounded corners and outline.
     *
     * @param g                `GuiGraphics` context to draw on
     * @param startPosX        Start X position
     * @param startPosY        Start Y position
     * @param endPosX          End X position
     * @param endPosY          End Y position
     * @param corners          `RoundedCorners` specifying which corners are rounded
     * @param outlineThickness Thickness of the outline in pixels
     * @param renderOutline    Whether to render the outline
     * @param backgroundColor  Background color
     * @param outlineColor     Outline color
     */
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
            g.fill(startPosX, startPosY, startPosX + outlineThickness, startPosY + outlineThickness, outlineColor.asInt());
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
            g.fill(endPosX - outlineThickness, startPosY, endPosX, startPosY + outlineThickness, outlineColor.asInt());
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
            g.fill(startPosX, endPosY - outlineThickness, startPosX + outlineThickness, endPosY, outlineColor.asInt());
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
            g.fill(endPosX - outlineThickness, endPosY - outlineThickness, endPosX, endPosY, outlineColor.asInt());
        }
    }

    /**
     * Draws a horizontal line with specified thickness and color.
     *
     * @param g         `GuiGraphics` context to draw on
     * @param startPosX Starting X position
     * @param endPosX   Ending X position
     * @param posY      Y position of the line
     * @param thickness Thickness of the line in pixels
     * @param color     Color of the line
     */
    public static void drawHorizontalLine(GuiGraphics g, int startPosX, int endPosX, int posY, int thickness, Color color)
    {
        g.fill(startPosX, posY, endPosX, posY + thickness, color.asInt());
    }

    /**
     * Draws a vertical line with specified thickness and color.
     *
     * @param g         `GuiGraphics` context to draw on
     * @param posX      X position of the line
     * @param startPosY Starting Y position
     * @param endPosY   Ending Y position
     * @param thickness Thickness of the line in pixels
     * @param color     Color of the line
     */
    public static void drawVerticalLine(GuiGraphics g, int posX, int startPosY, int endPosY, int thickness, Color color)
    {
        g.fill(posX, startPosY, posX + thickness, endPosY, color.asInt());
    }

    /**
     * Draws an item scaled by a given factor.
     *
     * @param scaleFactor Scaling factor (e.g. 1.0 = normal size, 2.0 = double size)
     */
    public static void drawScaledItemFactor(GuiGraphics g, ItemStack stack, int x, int y, float scaleFactor)
    {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0F);
        g.pose().scale(scaleFactor, scaleFactor, 1.0F);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();
    }

    /**
     * Draws an item scaled to an exact GUI pixel size.
     *
     * @param size Target size in GUI pixels (e.g. 24, 32, 48)
     */
    public static void drawScaledItemSize(GuiGraphics g, ItemStack stack, int x, int y, int size)
    {
        float scale = (float) size / (float) VANILLA_ITEM_SIZE;
        drawScaledItemFactor(g, stack, x, y, scale);
    }
}
