package com.drypted.spotlight.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class RendererUtils
{
    private static final int VANILLA_ITEM_SIZE = 16;

    /**
     * Draws an item scaled by a given factor.
     *
     * @param scale Scaling factor (e.g. 1.0 = normal size, 2.0 = double size)
     */
    public static void drawScaledItem(GuiGraphics g, ItemStack stack, int x, int y, float scale)
    {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0F);
        g.pose().scale(scale, scale, 1.0F);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();
    }

    /**
     * Draws an item scaled to an exact GUI pixel size.
     *
     * @param size Target size in GUI pixels (e.g. 24, 32, 48)
     */
    public static void drawScaledItem(GuiGraphics g, ItemStack stack, int x, int y, int size)
    {
        float scale = (float) size / (float) VANILLA_ITEM_SIZE;
        drawScaledItem(g, stack, x, y, scale);
    }
}
