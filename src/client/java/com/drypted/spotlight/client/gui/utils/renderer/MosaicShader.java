package com.drypted.spotlight.client.gui.utils.renderer;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.gui.utils.Color;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class MosaicShader
{
    // Uniform buffer layout (std140, all float):
    // offset  0: ScreenSize  (vec2)  = 8 bytes
    // offset  8: Region      (vec4)  = 16 bytes
    // offset 24: PixelSize   (float) = 4 bytes
    // offset 28: pad         (float) = 4 bytes  <- std140 alignment
    // offset 32: TintColor   (vec4)  = 16 bytes
    // total = 48 bytes
    private static final int UBO_SIZE = 48;

    public static final RenderPipeline MOSAIC_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(new RenderPipeline.Snippet[0])
                    .withLocation("pipeline/spotlight_mosaic")
                    .withVertexShader(Identifier.fromNamespaceAndPath(SpotlightEntryClient.MOD_ID, "mosaic_background"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(SpotlightEntryClient.MOD_ID, "mosaic_background"))
                    .withSampler("ScreenTexture")
                    .withUniform("MosaicData", UniformType.UNIFORM_BUFFER)
                    .withoutBlend()
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
                    .build()
    );

    private static GpuTexture snapshotTexture = null;
    private static GpuTextureView snapshotTextureView = null;
    private static GpuSampler snapshotSampler = null;
    private static int snapshotWidth = -1;
    private static int snapshotHeight = -1;

    private static GpuBuffer uniformBuffer = null;

    private static boolean hasCapturedThisFrame = false;
    private static boolean isDrawing = false;

    public static void captureFramebuffer(GuiGraphics g)
    {
        hasCapturedThisFrame = false;

        // In 1.21.11, GuiGraphics uses a deferred render state - no flush() needed.
        // The world/background is already rendered to the main framebuffer before the screen renders.

        Minecraft mc = Minecraft.getInstance();
        RenderTarget fb = mc.getMainRenderTarget();
        int fbW = fb.width;
        int fbH = fb.height;

        GpuDevice gpu = RenderSystem.getDevice();

        // (Re)allocate snapshot texture if resolution changed
        if (snapshotTexture == null || snapshotWidth != fbW || snapshotHeight != fbH)
        {
            freeTextures();

            snapshotTexture = gpu.createTexture(
                    "mosaic_snapshot",
                    GpuTexture.Usage.TEXTURE_BINDING | GpuTexture.Usage.COPY_DST,
                    TextureFormat.RGBA8,
                    fbW, fbH,
                    1,  // layers
                    1   // mip levels
            );
            snapshotTextureView = gpu.createTextureView(snapshotTexture);
            snapshotSampler = gpu.createSampler(
                    AddressMode.CLAMP_TO_EDGE,
                    AddressMode.CLAMP_TO_EDGE,
                    FilterMode.NEAREST,
                    FilterMode.NEAREST,
                    1,
                    OptionalDouble.empty()
            );
            snapshotWidth = fbW;
            snapshotHeight = fbH;
        }

        // GPU-side copy via CommandEncoder
        CommandEncoder encoder = gpu.createCommandEncoder();
        encoder.copyTextureToTexture(fb.getColorTexture(), snapshotTexture);
        encoder.close();

        hasCapturedThisFrame = true;
    }

    public static void draw(float pixelSize, int startX, int startY, int endX, int endY, Color color)
    {
        if (!hasCapturedThisFrame || isDrawing || snapshotTexture == null) return;

        int width  = endX - startX;
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
            float sW = (float) (width   * scale);
            float sH = (float) (height  * scale);
            float sp = pixelSize * (float) scale;

            // Upload uniforms into a GpuBuffer
            GpuDevice gpu = RenderSystem.getDevice();
            ByteBuffer data = ByteBuffer.allocate(UBO_SIZE).order(ByteOrder.nativeOrder());
            data.putFloat(0,  sw);
            data.putFloat(4,  sh);
            data.putFloat(8,  sx);
            data.putFloat(12, sy);
            data.putFloat(16, sW);
            data.putFloat(20, sH);
            data.putFloat(24, sp);
            data.putFloat(28, 0f); // padding
            data.putFloat(32, (float) color.getRed()   / 255f);
            data.putFloat(36, (float) color.getGreen() / 255f);
            data.putFloat(40, (float) color.getBlue()  / 255f);
            data.putFloat(44, (float) color.getAlpha() / 255f);
            data.rewind();

            if (uniformBuffer == null)
            {
                uniformBuffer = gpu.createBuffer(
                        () -> "mosaic_ubo",
                        GpuBuffer.Usage.UNIFORM | GpuBuffer.Usage.DYNAMIC,
                        UBO_SIZE
                );
            }

            CommandEncoder encoder = gpu.createCommandEncoder();
            encoder.writeToBuffer(uniformBuffer.slice(), data);

            RenderTarget mainTarget = mc.getMainRenderTarget();

            try (RenderPass pass = encoder.createRenderPass(
                    mainTarget.getColorTexture(),
                    OptionalInt.empty(),
                    null,
                    OptionalDouble.empty()
            ))
            {
                pass.setPipeline(MOSAIC_PIPELINE);
                pass.bindTexture("ScreenTexture", snapshotTextureView, snapshotSampler);
                pass.setUniform("MosaicData", uniformBuffer);

                // Build immediate vertex data into a temp GpuBuffer
                float x0 = startX, y0 = startY;
                float x1 = startX + width, y1 = startY + height;

                // POSITION format: 3 floats per vertex, 4 vertices
                ByteBuffer verts = ByteBuffer.allocate(4 * 3 * 4).order(ByteOrder.nativeOrder());
                verts.putFloat(x0); verts.putFloat(y0); verts.putFloat(0);
                verts.putFloat(x0); verts.putFloat(y1); verts.putFloat(0);
                verts.putFloat(x1); verts.putFloat(y1); verts.putFloat(0);
                verts.putFloat(x1); verts.putFloat(y0); verts.putFloat(0);
                verts.rewind();

                GpuBuffer vertexBuffer = gpu.createBuffer(
                        () -> "mosaic_verts",
                        GpuBuffer.Usage.VERTEX,
                        verts
                );

                pass.setVertexBuffer(0, vertexBuffer);
                pass.draw(0, 4);

                vertexBuffer.close();
            }

            encoder.close();
        }
        finally
        {
            isDrawing = false;
        }
    }

    public static void free()
    {
        freeTextures();
        if (uniformBuffer != null)
        {
            uniformBuffer.close();
            uniformBuffer = null;
        }
        hasCapturedThisFrame = false;
        isDrawing = false;
    }

    private static void freeTextures()
    {
        if (snapshotTextureView != null) { snapshotTextureView.close(); snapshotTextureView = null; }
        if (snapshotTexture != null)     { snapshotTexture.close();     snapshotTexture = null; }
        if (snapshotSampler != null)     { snapshotSampler.close();     snapshotSampler = null; }
        snapshotWidth  = -1;
        snapshotHeight = -1;
    }
}