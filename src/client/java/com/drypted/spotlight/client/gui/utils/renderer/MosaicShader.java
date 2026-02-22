package com.drypted.spotlight.client.gui.utils.renderer;

import com.drypted.spotlight.client.gui.utils.Color;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import java.io.IOException;
import java.util.Objects;

public final class MosaicShader
{
    private static ShaderInstance shader = null;
    private static boolean loadFailed = false;

    private static int snapshotTextureId = -1;
    private static int snapshotWidth = -1;
    private static int snapshotHeight = -1;

    // Re-entrancy guard: prevent nested mosaic draws
    private static boolean isDrawing = false;
    // Whether captureFramebuffer() succeeded this frame
    private static boolean hasCapturedThisFrame = false;

    public static void load() throws IOException
    {
        if (shader != null)
        {
            shader.close();
            shader = null;
        }
        loadFailed = false;
        try
        {
            shader = new ShaderInstance(
                    Minecraft.getInstance().getResourceManager(),
                    "mosaic_background",
                    DefaultVertexFormat.POSITION
            );
        }
        catch (IOException e)
        {
            loadFailed = true;
            throw e;
        }
    }

    public static boolean isAvailable()
    {
        return shader != null && !loadFailed;
    }

    /**
     * Call ONCE at the very start of SpotlightScreen.render(), before super.render().
     * Flushes pending draws then copies the framebuffer to the snapshot texture.
     */
    public static void captureFramebuffer(GuiGraphics g)
    {
        hasCapturedThisFrame = false;
        if (!isAvailable()) return;

        // Flush any pending vanilla batched draws so they appear in the FB
        g.flush();

        Minecraft mc = Minecraft.getInstance();
        RenderTarget fb = mc.getMainRenderTarget();
        int fbW = fb.width;
        int fbH = fb.height;

        // (Re)allocate snapshot texture if resolution changed
        if (snapshotTextureId == -1 || snapshotWidth != fbW || snapshotHeight != fbH)
        {
            if (snapshotTextureId != -1) GlStateManager._deleteTexture(snapshotTextureId);

            snapshotTextureId = GlStateManager._genTexture();

            // Bind directly via GL — bypasses MC's state tracker entirely
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, snapshotTextureId);
            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL11.GL_RGBA8,
                    fbW,
                    fbH,
                    0,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    (java.nio.ByteBuffer) null
            );
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            snapshotWidth = fbW;
            snapshotHeight = fbH;
        }

        // Copy FB colour → snapshot (pure GPU blit, no CPU readback)
        fb.bindRead();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, snapshotTextureId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, fbW, fbH);

        // Restore write target
        fb.bindWrite(false);

        hasCapturedThisFrame = true;
    }

    /**
     * Draw mosaic background for one widget. Safe to call multiple times per frame.
     * Does nothing if captureFramebuffer() hasn't been called this frame,
     * or if we're already inside a draw() call (re-entrancy guard).
     */
    public static void draw(float pixelSize, int startX, int startY, int endX, int endY, Color color)
    {
        if (!isAvailable() || !hasCapturedThisFrame || isDrawing) return;
        if (snapshotTextureId == -1) return;

        int width = endX - startX;
        int height = endY - startY;

        isDrawing = true;
        try
        {
            Minecraft mc = Minecraft.getInstance();
            double scale = mc.getWindow().getGuiScale();
            float sw = snapshotWidth;
            float sh = snapshotHeight;

            float sx = (float) (startX * scale);
            float sy = (float) (startY * scale);
            float sW = (float) (width * scale);
            float sH = (float) (height * scale);
            float sp = pixelSize * (float) scale;

            // Manually activate texture unit 0 and bind our snapshot,
            // bypassing RenderSystem's state cache (which causes the "unloadable" warning)
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, snapshotTextureId);

            RenderSystem.setShader(() -> shader);

            shader.safeGetUniform("ScreenSize").set(sw, sh);
            shader.safeGetUniform("Region").set(sx, sy, sW, sH);
            shader.safeGetUniform("PixelSize").set(sp);
            shader.safeGetUniform("TintColor").set(
                    (float) color.getRed() / 255f,
                    (float) color.getGreen() / 255f,
                    (float) color.getBlue() / 255f,
                    (float) color.getAlpha() / 255f
            );
            shader.safeGetUniform("ModelViewMat").set(RenderSystem.getModelViewMatrix());
            shader.safeGetUniform("ProjMat").set(RenderSystem.getProjectionMatrix());

            shader.apply();

            BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            buf.addVertex(startX, startY, 0);
            buf.addVertex(startX, startY + height, 0);
            buf.addVertex(startX + width, startY + height, 0);
            buf.addVertex(startX + width, startY, 0);

            BufferUploader.drawWithShader(Objects.requireNonNull(buf.build()));
            shader.clear();

            // Restore MC's texture state so subsequent vanilla draws aren't broken
            RenderSystem.bindTextureForSetup(0);
        }
        finally
        {
            isDrawing = false;
        }
    }

    /**
     * Call when the screen closes or the game shuts down.
     */
    public static void free()
    {
        if (shader != null)
        {
            shader.close();
            shader = null;
        }
        if (snapshotTextureId != -1)
        {
            GlStateManager._deleteTexture(snapshotTextureId);
            snapshotTextureId = -1;
        }
        hasCapturedThisFrame = false;
        isDrawing = false;
    }
}