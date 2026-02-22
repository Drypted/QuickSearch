package com.drypted.spotlight.client.gui.utils.renderer;

import com.drypted.spotlight.client.gui.models.RoundedCorners;
import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;

public final class RenderUtils
{
    private static final int VANILLA_ITEM_SIZE = 16;
    private static final float BACKGROUND_PIXEL_SIZE = 8f;

    /**
     * Fills a rectangle with optional rounded corners and outline.
     *
     * @param g               `GuiGraphics` context to draw on
     * @param startPosX       Start X position
     * @param startPosY       Start Y position
     * @param endPosX         End X position
     * @param endPosY         End Y position
     * @param corners         `RoundedCorners` specifying which corners are rounded; requires insetThickness > 0
     * @param insetThickness  Thickness of the outline in pixels
     * @param renderOutline   Whether to render the outline
     * @param backgroundColor Background color
     * @param outlineColor    Outline color
     */
    public static void drawRectangle(GuiGraphics g, int startPosX, int startPosY, int endPosX, int endPosY, RoundedCorners corners, int insetThickness, boolean renderOutline, Color backgroundColor, Color outlineColor)
    {
        insetThickness = Math.max(0, insetThickness);

        // Main body
        if (!renderOutline || insetThickness == 0)
        {
            if (MosaicShader.isAvailable()) MosaicShader.draw(
                    BACKGROUND_PIXEL_SIZE,
                    startPosX,
                    startPosY,
                    endPosX,
                    endPosY,
                    backgroundColor
            );
            return;
        }

        if (MosaicShader.isAvailable()) MosaicShader.draw(
                BACKGROUND_PIXEL_SIZE,
                startPosX + insetThickness,
                startPosY + insetThickness,
                endPosX - insetThickness,
                endPosY - insetThickness,
                backgroundColor
        );

        // LEFT
        g.fill(
                startPosX,
                startPosY + (corners.topLeft() ? insetThickness : 0),
                startPosX + insetThickness,
                endPosY - (corners.bottomLeft() ? insetThickness : 0),
                outlineColor.asInt()
        );

        // RIGHT
        g.fill(
                endPosX - insetThickness,
                startPosY + (corners.topRight() ? insetThickness : 0),
                endPosX,
                endPosY - (corners.bottomRight() ? insetThickness : 0),
                outlineColor.asInt()
        );

        // TOP
        g.fill(
                startPosX + (corners.topLeft() ? insetThickness : 0),
                startPosY,
                endPosX - (corners.topRight() ? insetThickness : 0),
                startPosY + insetThickness,
                outlineColor.asInt()
        );

        // BOTTOM
        g.fill(
                startPosX + (corners.bottomLeft() ? insetThickness : 0),
                endPosY - insetThickness,
                endPosX - (corners.bottomRight() ? insetThickness : 0),
                endPosY,
                outlineColor.asInt()
        );

        // Corner pixels for rounded

        if (corners.topLeft())
        {
            g.fill(
                    startPosX + insetThickness,
                    startPosY + insetThickness,
                    startPosX + (2 * insetThickness),
                    startPosY + (2 * insetThickness),
                    outlineColor.asInt()
            );
        }

        if (corners.topRight())
        {
            g.fill(
                    endPosX - (2 * insetThickness),
                    startPosY + insetThickness,
                    endPosX - insetThickness,
                    startPosY + (2 * insetThickness),
                    outlineColor.asInt()
            );
        }

        if (corners.bottomLeft())
        {
            g.fill(
                    startPosX + insetThickness,
                    endPosY - (2 * insetThickness),
                    startPosX + (2 * insetThickness),
                    endPosY - insetThickness,
                    outlineColor.asInt()
            );
        }

        if (corners.bottomRight())
        {
            g.fill(
                    endPosX - (2 * insetThickness),
                    endPosY - (2 * insetThickness),
                    endPosX - insetThickness,
                    endPosY - insetThickness,
                    outlineColor.asInt()
            );
        }
    }

    /* LOADERS */

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
        drawThickLine(guiGraphics, shortArmStartX, shortArmStartY, shortArmEndX, shortArmEndY, thickness, color);

        // Draw long arm (with thickness)
        drawThickLine(guiGraphics, shortArmEndX, shortArmEndY, longArmEndX, longArmEndY, thickness, color);
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
            Color dotColor = color.withOpacity((int) (255 * alpha));

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
            Color dotColor = color.withOpacity((int) (255 * alpha));

            // Draw dot
            int dotX = startX + (i * spacing) - (dotSize - baseDotSize) / 2;
            int dotY = centerY - (int) bounce - dotSize / 2;

            guiGraphics.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, dotColor.asInt());
        }
    }

    /**
     * Draws an X
     *
     * @param guiGraphics  `GuiGraphics` context to draw on
     * @param startX       Start X position
     * @param startY       Start Y position
     * @param endX         End X position
     * @param endY         End Y position
     * @param color        Color of the X
     * @param thickness    Thickness of the X lines in pixels
     * @param drawShadow   Whether to draw a shadow
     * @param shadowOffset Offset of the shadow in pixels (allows floats)
     */
    public static void drawX(GuiGraphics guiGraphics, int startX, int startY, int endX, int endY, Color color, int thickness, boolean drawShadow, float shadowOffset)
    {
        if (drawShadow)
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(thickness * shadowOffset, thickness * shadowOffset, 0.0f);

            drawThickLine(guiGraphics, startX, startY, endX, endY, thickness, Colors.SHADOW);
            drawThickLine(guiGraphics, startX, endY, endX, startY, thickness, Colors.SHADOW);

            guiGraphics.pose().popPose();
        }

        drawThickLine(guiGraphics, startX, startY, endX, endY, thickness, color);
        drawThickLine(guiGraphics, startX, endY, endX, startY, thickness, color);
    }

    /* LABEL */

    public static void drawLabel(GuiGraphics g, String text, int posX, int posY, float scale, int paddingX, int paddingY, RoundedCorners rounded, Color backgroundColor, Color outlineColor, Color textColor)
    {
        final int fontWidth = (int) ((float) Minecraft.getInstance().font.width(text) * scale);
        final int fontHeight = (int) ((float) Minecraft.getInstance().font.lineHeight * scale);
        RenderUtils.drawRectangle(
                g,
                posX - paddingX,
                posY - paddingY,
                posX + fontWidth + paddingX,
                posY + fontHeight + paddingY,
                rounded,
                1,
                !outlineColor.equals(Colors.CLEAR), // draw outline if its not clear
                backgroundColor,
                outlineColor
        );

        RenderUtils.drawScaledText(g, text, scale, posX, posY, textColor);
    }

    public static void drawLabelWithScale(GuiGraphics g, String text, float scale, int startX, int startY, int endX, int endY, RoundedCorners corners, Color backgroundColor, Color textColor)
    {
        // background
        RenderUtils.drawRectangle(g, startX, startY, endX, endY, corners, 1, true, backgroundColor, backgroundColor);

        // text
        int textWidth = (int) (Minecraft.getInstance().font.width(text) * scale);
        int textHeight = (int) (Minecraft.getInstance().font.lineHeight * scale);

        int textPosX = startX + ((endX - startX) / 2) - (textWidth / 2);
        int textPosY = startY + (endY - startY) / 2 - (textHeight / 2);

        RenderUtils.drawScaledText(g, text, scale, textPosX, textPosY, textColor);
    }

    public static void drawLabelInBox(GuiGraphics g, String text, int padding, int startX, int startY, int endX, int endY, RoundedCorners corners, Color backgroundColor, Color textColor)
    {
        RenderUtils.drawRectangle(g, startX, startY, endX, endY, corners, 1, true, backgroundColor, backgroundColor);

        int boxWidth = (endX - startX) - (padding * 2);
        int boxHeight = (endY - startY) - (padding * 2);

        int textWidth = Minecraft.getInstance().font.width(text);
        int textHeight = Minecraft.getInstance().font.lineHeight;

        if (textWidth <= 0 || boxWidth <= 0 || boxHeight <= 0)
        {
            return;
        }

        float scaleX = (float) boxWidth / textWidth;
        float scaleY = (float) boxHeight / textHeight;
        float scale = Math.min(scaleX, scaleY);

        int scaledTextWidth = (int) (textWidth * scale);
        int scaledTextHeight = (int) (textHeight * scale);

        int textX = startX + padding + ((boxWidth - scaledTextWidth) / 2);
        int textY = startY + padding + ((boxHeight - scaledTextHeight) / 2);

        drawScaledText(g, text, scale, textX, textY, textColor);
    }

    /* DEBUG */

    public static void __debugDrawWidgetBounds(GuiGraphics g, AbstractWidget widget)
    {
        __debugDrawWidgetBounds(g, widget, true);
    }

    public static void __debugDrawWidgetBounds(GuiGraphics g, AbstractWidget widget, boolean above)
    {
        g.pose().pushPose();
        g.pose().translate(0, 0, above ? 100 : -100); // set z order
        drawRectangle(
                g,
                widget.getX(),
                widget.getY(),
                widget.getRight(),
                widget.getBottom(),
                RoundedCorners.none(),
                1,
                true,
                Colors.DEBUG_RECT_FILL,
                Colors.DEBUG_RECT_OUTLINE
        );
        g.pose().popPose();
    }

    /* MINI-FUNCTIONS */

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

    /* MARK: PRIVATE */

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
        if (thickness <= 0) return;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0.0f) return;

        float nx = -dy / length;
        float ny = dx / length;
        float half = thickness * 0.5f;

        float x1a = x1 + nx * half;
        float y1a = y1 + ny * half;
        float x1b = x1 - nx * half;
        float y1b = y1 - ny * half;
        float x2a = x2 + nx * half;
        float y2a = y2 + ny * half;
        float x2b = x2 - nx * half;
        float y2b = y2 - ny * half;

        fillQuad(guiGraphics, x1a, y1a, x2a, y2a, x2b, y2b, x1b, y1b, color.asInt());
    }

    /**
     * Fills a convex quadrilateral using scanline rasterization.
     */
    private static void fillQuad(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color)
    {
        int minY = (int) Math.floor(Math.min(Math.min(y1, y2), Math.min(y3, y4)));
        int maxY = (int) Math.ceil(Math.max(Math.max(y1, y2), Math.max(y3, y4)));

        for (int y = minY; y <= maxY; y++)
        {
            float[] xs = new float[4];
            int count = 0;

            count = intersect(xs, count, x1, y1, x2, y2, y);
            count = intersect(xs, count, x2, y2, x3, y3, y);
            count = intersect(xs, count, x3, y3, x4, y4, y);
            count = intersect(xs, count, x4, y4, x1, y1, y);

            if (count < 2) continue;

            float minX = xs[0];
            float maxX = xs[1];

            if (minX > maxX)
            {
                float tmp = minX;
                minX = maxX;
                maxX = tmp;
            }

            guiGraphics.fill((int) Math.floor(minX), y, (int) Math.ceil(maxX), y + 1, color);
        }
    }

    private static int intersect(float[] xs, int count, float x1, float y1, float x2, float y2, int y)
    {
        if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y))
        {
            float t = (y - y1) / (y2 - y1);
            xs[count++] = x1 + t * (x2 - x1);
        }
        return count;
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

            if (x1 == x2 && y1 == y2) break;

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

    private static void drawScaledText(GuiGraphics g, String text, float scale, int x, int y, Color color)
    {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0F);
        g.pose().scale(scale, scale, 1.0F);
        g.drawString(Minecraft.getInstance().font, text, 0, 0, color.asInt());
        g.pose().popPose();
    }

    /* OVERLOADS */

    /**
     * Draws an X
     *
     * @param guiGraphics `GuiGraphics` context to draw on
     * @param startX      Start X position
     * @param startY      Start Y position
     * @param endX        End X position
     * @param endY        End Y position
     * @param color       color of the X
     * @param thickness   Thickness of the X lines in pixels
     * @param drawShadow  Whether to draw a shadow
     */
    public static void drawX(GuiGraphics guiGraphics, int startX, int startY, int endX, int endY, Color color, int thickness, boolean drawShadow)
    {
        drawX(guiGraphics, startX, startY, endX, endY, color, thickness, drawShadow, 0.5F);
    }
}
