package com.drypted.spotlight.client.gui.utils.renderer;

import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import net.minecraft.client.Minecraft;
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
    public static void drawRectangle(GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY, RoundedCorners corners, int outlineThickness, boolean renderOutline, Color backgroundColor, Color outlineColor)
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
            g.fill(
                    startPosX,
                    startPosY,
                    startPosX + outlineThickness,
                    startPosY + outlineThickness,
                    outlineColor.asInt()
            );
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
            g.fill(
                    endPosX - outlineThickness,
                    startPosY,
                    endPosX,
                    startPosY + outlineThickness,
                    outlineColor.asInt()
            );
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
            g.fill(
                    startPosX,
                    endPosY - outlineThickness,
                    startPosX + outlineThickness,
                    endPosY,
                    outlineColor.asInt()
            );
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
            g.fill(
                    endPosX - outlineThickness,
                    endPosY - outlineThickness,
                    endPosX,
                    endPosY,
                    outlineColor.asInt()
            );
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
     * @param color     color of the line
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
     * @param color     color of the line
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

    /**
     * Draws a checkmark/tick icon.
     *
     * @param guiGraphics `GuiGraphics` context to draw on
     * @param posX        X position of the tick's bounding box
     * @param posY        Y position of the tick's bounding box
     * @param size        Size of the tick's bounding box in pixels
     * @param color       color of the tick
     * @param thickness   Thickness of the tick lines in pixels
     */
    public static void drawTick(GuiGraphics guiGraphics, int posX, int posY, int size, Color color, int thickness)
    {
        thickness = Math.max(1, thickness);

        // Calculate tick geometry (proportional to size)
        // Short arm: bottom-left to middle
        int shortArmStartX = posX + (int) (size * 0.2f);
        int shortArmStartY = posY + (int) (size * 0.5f);
        int shortArmEndX = posX + (int) (size * 0.4f);
        int shortArmEndY = posY + (int) (size * 0.7f);

        // Long arm: middle to top-right
        // start from short arm end
        int longArmEndX = posX + (int) (size * 0.8f);
        int longArmEndY = posY + (int) (size * 0.3f);

        // Draw short arm (with thickness)
        drawThickLine(
                guiGraphics,
                shortArmStartX,
                shortArmStartY,
                shortArmEndX,
                shortArmEndY,
                thickness,
                color
        );

        // Draw long arm (with thickness)
        drawThickLine(
                guiGraphics,
                shortArmEndX,
                shortArmEndY,
                longArmEndX,
                longArmEndY,
                thickness,
                color
        );
    }

    /**
     * Draws a three-dot loading animation (dots pulse in sequence).
     *
     * @param guiGraphics   `GuiGraphics` context to draw on
     * @param posX          X position of the animation's bounding box
     * @param posY          Y position of the animation's bounding box
     * @param size          Size of the animation's bounding box in pixels
     * @param color         color of the dots
     * @param currentTimeMs Current time in milliseconds for animation
     */
    public static void drawThreeDotPulseSpinner(GuiGraphics guiGraphics, int posX, int posY, int size, Color color, long currentTimeMs)
    {
        final int dotCount = 3;
        final int animationCycle = 900; // milliseconds for one complete cycle
        final int dotSize = Math.max(2, size / 5); // Size of each dot
        final int spacing = size / 4; // Spacing between dots

        // Calculate total width of all dots
        int totalWidth = (dotCount - 1) * spacing + dotSize;
        int startX = posX + (size - totalWidth) / 2;
        int centerY = posY + size / 2 - dotSize / 2;

        for (int i = 0; i < dotCount; i++)
        {
            // Each dot starts its animation with a delay
            int dotDelay = i * (animationCycle / dotCount);
            long adjustedTime = (currentTimeMs + dotDelay) % animationCycle;
            float progress = adjustedTime / (float) animationCycle;

            // Calculate opacity using sine wave (0.3 to 1.0)
            float alpha = 0.3f + 0.7f * (float) Math.abs(Math.sin(progress * Math.PI * 2));
            Color dotColor = color.withAlpha((int) (255 * alpha));

            // Draw dot
            int dotX = startX + (i * spacing);
            guiGraphics.fill(dotX, centerY, dotX + dotSize, centerY + dotSize, dotColor.asInt());
        }
    }

    /**
     * Draws a three-dot loading animation (dots bounce and pulse).
     *
     * @param guiGraphics   `GuiGraphics` context to draw on
     * @param posX          X position of the animation's bounding box
     * @param posY          Y position of the animation's bounding box
     * @param size          Size of the animation's bounding box in pixels
     * @param color         color of the dots
     * @param currentTimeMs Current time in milliseconds for animation
     */
    public static void drawThreeDotBouncePulseSpinner(GuiGraphics guiGraphics, int posX, int posY, int size, Color color, long currentTimeMs)
    {
        final int dotCount = 3;
        final int animationCycle = 800;
        final int baseDotSize = Math.max(2, size / 6);
        final int spacing = size / 4;
        final float bounceHeight = size / 3.5f;

        int totalWidth = (dotCount - 1) * spacing + baseDotSize;
        int startX = posX + (size - totalWidth) / 2;
        int centerY = posY + size / 2;

        for (int i = 0; i < dotCount; i++)
        {
            int dotDelay = i * (animationCycle / dotCount);
            long adjustedTime = (currentTimeMs + dotDelay) % animationCycle;
            float progress = adjustedTime / (float) animationCycle;
            float waveValue = (float) Math.sin(progress * Math.PI * 2);

            // Bounce effect
            float bounce = Math.abs(waveValue) * bounceHeight;

            // Scale effect (dots get slightly bigger when at peak)
            float scale = 1.0f + Math.abs(waveValue) * 0.3f;
            int dotSize = (int) (baseDotSize * scale);

            // Opacity effect
            float alpha = 0.5f + 0.5f * Math.abs(waveValue);
            Color dotColor = color.withAlpha((int) (255 * alpha));

            // Draw dot
            int dotX = startX + (i * spacing) - (dotSize - baseDotSize) / 2;
            int dotY = centerY - (int) bounce - dotSize / 2;

            guiGraphics.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, dotColor.asInt());
        }
    }

    /**
     * Helper method to draw a thick line between two points.
     *
     * @param guiGraphics `GuiGraphics` context to draw on
     * @param x1          Start X position
     * @param y1          Start Y position
     * @param x2          End X position
     * @param y2          End Y position
     * @param thickness   Thickness of the line in pixels
     * @param color       color of the line
     */
    private static void drawThickLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int thickness, Color color)
    {
        // Calculate perpendicular offset for thickness
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0)
            return;

        float perpX = -dy / length * thickness / 2.0f;
        float perpY = dx / length * thickness / 2.0f;

        // Draw filled polygon (thick line)
        int colorInt = color.asInt();

        for (int t = 0; t < thickness; t++)
        {
            float offset = t - thickness / 2.0f;
            float offsetX = -dy / length * offset;
            float offsetY = dx / length * offset;

            int startX = (int) (x1 + offsetX);
            int startY = (int) (y1 + offsetY);
            int endX = (int) (x2 + offsetX);
            int endY = (int) (y2 + offsetY);

            // Use Bresenham's line algorithm for pixel-perfect rendering
            drawBresenhamLine(guiGraphics, startX, startY, endX, endY, colorInt);
        }
    }

    /**
     * Draws a line using Bresenham's algorithm.
     */
    private static void drawBresenhamLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color)
    {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true)
        {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2)
                break;

            int e2 = 2 * err;
            if (e2 > -dy)
            {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx)
            {
                err += dx;
                y1 += sy;
            }
        }
    }

    public static void drawText(GuiGraphics guiGraphics, String text, int posX, int posY)
    {
        drawText(
                guiGraphics,
                text,
                posX,
                posY,
                4,
                Colors.BLACK.withHalfAlpha(),
                Colors.WHITE,
                Colors.WHITE
        );
    }

    public static void drawText(GuiGraphics guiGraphics, String text, int posX, int posY, int padding, Color backgroundColor, Color outlineColor, Color textColor)
    {
        RenderUtils.drawRectangle(
                guiGraphics,
                posX - padding,
                posY - padding,
                posX + Minecraft.getInstance().font.width(text) + padding,
                posY + Minecraft.getInstance().font.lineHeight + padding,
                RoundedCorners.all(),
                1,
                true,
                backgroundColor,
                outlineColor
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                text,
                posX,
                posY,
                textColor.asInt(),
                true
        );
    }
}
