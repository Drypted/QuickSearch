package com.drypted.spotlight.client.ui.renderer;

import com.drypted.spotlight.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.spotlight.client.core.blueprints.ui.common.Color;
import com.drypted.spotlight.client.core.blueprints.ui.common.Colors;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

public final class RenderCommon
{
    private static final int VANILLA_ITEM_SIZE = 16;

    /**
     * Fills a rectangle with optional rounded corners and outline.
     *
     * @param g               `GuiGraphics` context to draw on
     * @param startPosX       Start X position
     * @param startPosY       Start Y position
     * @param endPosX         End X position
     * @param endPosY         End Y position
     * @param corners         `RoundedCorners` specifying which corners are rounded; requires insetThickness > 0
     * @param insetThickness  Thickness of the outline in pixels (Can be fractional)
     * @param renderOutline   Whether to render the outline
     * @param backgroundColor Background color
     * @param outlineColor    Outline color
     */
    public static void drawRectangle(GuiGraphics g, float startPosX, float startPosY, float endPosX, float endPosY, RoundedCorners corners, float insetThickness, boolean renderOutline, Color backgroundColor, Color outlineColor)
    {
        insetThickness = Math.max(0f, insetThickness);

        if (insetThickness == 0f)
        {
            drawQuad(g, startPosX, startPosY, endPosX, endPosY, backgroundColor);
            return;
        }

        float innerLeft = startPosX + insetThickness;
        float innerTop = startPosY + insetThickness;
        float innerRight = endPosX - insetThickness;
        float innerBottom = endPosY - insetThickness;

        // Inner body
        drawQuad(g, innerLeft, innerTop, innerRight, innerBottom, backgroundColor);

        Color outerLineColor = renderOutline ? outlineColor : backgroundColor;

        // LEFT
        drawQuad(
                g,
                startPosX,
                startPosY + (corners.topLeft() ? insetThickness : 0f),
                startPosX + insetThickness,
                endPosY - (corners.bottomLeft() ? insetThickness : 0f),
                outerLineColor
        );

        // RIGHT
        drawQuad(
                g,
                endPosX - insetThickness,
                startPosY + (corners.topRight() ? insetThickness : 0f),
                endPosX,
                endPosY - (corners.bottomRight() ? insetThickness : 0f),
                outerLineColor
        );

        // TOP
        drawQuad(
                g,
                startPosX + (corners.topLeft() ? insetThickness : 0f),
                startPosY,
                endPosX - (corners.topRight() ? insetThickness : 0f),
                startPosY + insetThickness,
                outerLineColor
        );

        // BOTTOM
        drawQuad(
                g,
                startPosX + (corners.bottomLeft() ? insetThickness : 0f),
                endPosY - insetThickness,
                endPosX - (corners.bottomRight() ? insetThickness : 0f),
                endPosY,
                outerLineColor
        );

        // CORNER INNER PIXELS (when outline shown)
        if (renderOutline && corners.topLeft())
        {
            drawQuad(g, innerLeft, innerTop, innerLeft + insetThickness, innerTop + insetThickness, outerLineColor);
        }
        if (renderOutline && corners.topRight())
        {
            drawQuad(g, innerRight - insetThickness, innerTop, innerRight, innerTop + insetThickness, outerLineColor);
        }
        if (renderOutline && corners.bottomLeft())
        {
            drawQuad(
                    g,
                    innerLeft,
                    innerBottom - insetThickness,
                    innerLeft + insetThickness,
                    innerBottom,
                    outerLineColor
            );
        }
        if (renderOutline && corners.bottomRight())
        {
            drawQuad(
                    g,
                    innerRight - insetThickness,
                    innerBottom - insetThickness,
                    innerRight,
                    innerBottom,
                    outerLineColor
            );
        }
    }

    private static void drawQuad(GuiGraphics g, float x1, float y1, float x2, float y2, Color color)
    {
        drawFloatQuad(g, x1, y1, x2, y1, x2, y2, x1, y2, color.asInt());
    }

    /**
     * Submits an arbitrary float-coordinate quad to the GUI render pipeline. Vertices are in order: top-left,
     * top-right, bottom-right, bottom-left.
     */
    private static void drawFloatQuad(GuiGraphics g, float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, int color)
    {
        Matrix3x2fc pose = new Matrix3x2f(g.pose());
        ScreenRectangle scissor = g.scissorStack.peek();
        ScreenRectangle bounds = computeBounds(pose, ax, ay, bx, by, cx, cy, dx, dy, scissor);

        g.guiRenderState.submitGuiElement(new GuiElementRenderState()
        {
            @Override
            public void buildVertices(@NonNull VertexConsumer consumer)
            {
                consumer.addVertexWith2DPose(pose, ax, ay).setColor(color);
                consumer.addVertexWith2DPose(pose, dx, dy).setColor(color);
                consumer.addVertexWith2DPose(pose, cx, cy).setColor(color);
                consumer.addVertexWith2DPose(pose, bx, by).setColor(color);
            }

            @Override
            public @NonNull RenderPipeline pipeline() { return RenderPipelines.GUI; }

            @Override
            public @NonNull TextureSetup textureSetup() { return TextureSetup.noTexture(); }

            @Override
            public ScreenRectangle scissorArea() { return scissor; }

            @Override
            public ScreenRectangle bounds() { return bounds; }
        });
    }

    private static ScreenRectangle computeBounds(Matrix3x2fc pose, float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, ScreenRectangle scissor)
    {
        Vector2f pa = pose.transformPosition(ax, ay, new Vector2f());
        Vector2f pb = pose.transformPosition(bx, by, new Vector2f());
        Vector2f pc = pose.transformPosition(cx, cy, new Vector2f());
        Vector2f pd = pose.transformPosition(dx, dy, new Vector2f());

        float minX = Math.min(Math.min(pa.x, pb.x), Math.min(pc.x, pd.x));
        float maxX = Math.max(Math.max(pa.x, pb.x), Math.max(pc.x, pd.x));
        float minY = Math.min(Math.min(pa.y, pb.y), Math.min(pc.y, pd.y));
        float maxY = Math.max(Math.max(pa.y, pb.y), Math.max(pc.y, pd.y));

        ScreenRectangle rect = new ScreenRectangle(
                Mth.floor(minX),
                Mth.floor(minY),
                Mth.ceil(maxX - minX),
                Mth.ceil(maxY - minY)
        );
        return scissor != null ? scissor.intersection(rect) : rect;
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
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(thickness * shadowOffset, thickness * shadowOffset);

            drawRasterLine(guiGraphics, startX, startY, endX, endY, thickness, Colors.SHADOW);
            drawRasterLine(guiGraphics, startX, endY, endX, startY, thickness, Colors.SHADOW);

            guiGraphics.pose().popMatrix();
        }

        drawRasterLine(guiGraphics, startX, startY, endX, endY, thickness, color);
        drawRasterLine(guiGraphics, startX, endY, endX, startY, thickness, color);
    }

    /* LABEL */

    public static void drawLabel(GuiGraphics g, String text, int posX, int posY, float scale, int paddingX, int paddingY, RoundedCorners rounded, Color backgroundColor, Color outlineColor, Color textColor)
    {
        final int fontWidth = (int) ((float) Minecraft.getInstance().font.width(text) * scale);
        final int fontHeight = (int) ((float) Minecraft.getInstance().font.lineHeight * scale);
        RenderCommon.drawRectangle(
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

        RenderCommon.drawScaledText(g, text, scale, posX, posY, textColor);
    }

    public static void drawLabelWithScale(GuiGraphics g, String text, float scale, int startX, int startY, int endX, int endY, RoundedCorners corners, float insetThickness, Color backgroundColor, Color textColor)
    {
        // background
        RenderCommon.drawRectangle(
                g,
                startX,
                startY,
                endX,
                endY,
                corners,
                insetThickness,
                true,
                backgroundColor,
                backgroundColor
        );

        // text
        int textWidth = (int) (Minecraft.getInstance().font.width(text) * scale);
        int textHeight = (int) (Minecraft.getInstance().font.lineHeight * scale);

        int textPosX = startX + ((endX - startX) / 2) - (textWidth / 2);
        int textPosY = startY + (endY - startY) / 2 - (textHeight / 2);

        RenderCommon.drawScaledText(g, text, scale, textPosX, textPosY, textColor);
    }

    public static void drawLabelInBox(GuiGraphics g, String text, int padding, int startX, int startY, int endX, int endY, RoundedCorners corners, Color backgroundColor, Color textColor)
    {
        RenderCommon.drawRectangle(g, startX, startY, endX, endY, corners, 1, true, backgroundColor, backgroundColor);

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
        g.pose().pushMatrix();
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
        g.pose().popMatrix();
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
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scaleFactor, scaleFactor);
        g.renderItem(stack, 0, 0);
        g.pose().popMatrix();
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

    public static void drawScaledText(GuiGraphics g, String text, float scale, int x, int y, Color color, boolean drawShadow)
    {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawString(Minecraft.getInstance().font, text, 0, 0, color.asInt(), drawShadow);
        g.pose().popMatrix();
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
     * Fills a convex quadrilateral with float precision using the GUI render pipeline. Vertices in order: (x1,y1),
     * (x2,y2), (x3,y3), (x4,y4) — must be convex and wound correctly.
     */
    private static void fillQuad(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color)
    {
        drawFloatQuad(guiGraphics, x1, y1, x2, y2, x3, y3, x4, y4, color);
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

    /**
     * Draws a thick line using Bresenham sampling and a square brush.
     */
    private static void drawRasterLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int thickness, Color color)
    {
        if (thickness <= 0) return;

        final int brushStart = -((thickness - 1) / 2);
        final int brushEnd = brushStart + thickness;
        final int argb = color.asInt();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true)
        {
            for (int offsetX = brushStart; offsetX < brushEnd; offsetX++)
            {
                for (int offsetY = brushStart; offsetY < brushEnd; offsetY++)
                {
                    guiGraphics.fill(x1 + offsetX, y1 + offsetY, x1 + offsetX + 1, y1 + offsetY + 1, argb);
                }
            }

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
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawString(Minecraft.getInstance().font, text, 0, 0, color.asInt());
        g.pose().popMatrix();
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
