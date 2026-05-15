package com.drypted.spotlight.client.ui.renderer;

import com.drypted.spotlight.client.SpotlightClient;
import com.drypted.spotlight.client.mixin.GameRendererAccessor;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public final class MosaicBackgroundRenderer
{
    private static final Identifier POST_CHAIN_ID = Identifier.fromNamespaceAndPath(
            SpotlightClient.MOD_ID,
            "mosaic_background"
    );
    private static final Identifier MOSAIC_TARGET_ID = Identifier.fromNamespaceAndPath(
            SpotlightClient.MOD_ID,
            "mosaic_output"
    );
    private static final Set<Identifier> POST_CHAIN_TARGETS = Set.of(PostChain.MAIN_TARGET_ID, MOSAIC_TARGET_ID);

    private static TextureTarget mosaicTarget;
    private static int mosaicWidth = -1;
    private static int mosaicHeight = -1;
    private static boolean capturedThisFrame = false;

    private MosaicBackgroundRenderer() { }

    public static void captureFramebuffer()
    {
        capturedThisFrame = false;

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        if (mainTarget.getColorTexture() == null)
        {
            return;
        }

        int width = mainTarget.width;
        int height = mainTarget.height;
        ensureTargets(width, height);
        if (mosaicTarget == null || mosaicTarget.getColorTextureView() == null)
        {
            return;
        }

        PostChain postChain = minecraft.getShaderManager().getPostChain(POST_CHAIN_ID, POST_CHAIN_TARGETS);
        if (postChain == null)
        {
            return;
        }

        GraphicsResourceAllocator resourcePool = ((GameRendererAccessor) minecraft.gameRenderer).spotlight$getResourcePool();
        FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
        ResourceHandle<RenderTarget> mainHandle = frameGraphBuilder.importExternal("main", mainTarget);
        ResourceHandle<RenderTarget> mosaicHandle = frameGraphBuilder.importExternal("spotlight_mosaic", mosaicTarget);

        postChain.addToFrame(frameGraphBuilder, width, height, new MosaicTargetBundle(mainHandle, mosaicHandle));
        frameGraphBuilder.execute(resourcePool);
        capturedThisFrame = true;
    }

    public static boolean drawMosaic(GuiGraphics guiGraphics, float startPosX, float startPosY, float endPosX, float endPosY)
    {
        if (!capturedThisFrame || mosaicTarget == null || mosaicTarget.getColorTextureView() == null)
        {
            return false;
        }

        if (endPosX <= startPosX || endPosY <= startPosY)
        {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float guiWidth = minecraft.getWindow().getGuiScaledWidth();
        float guiHeight = minecraft.getWindow().getGuiScaledHeight();

        float u0 = startPosX / guiWidth;
        float u1 = endPosX / guiWidth;
        float v0 = 1.0f - startPosY / guiHeight;
        float v1 = 1.0f - endPosY / guiHeight;

        ScreenRectangle scissor = guiGraphics.scissorStack.peek();
        guiGraphics.guiRenderState.submitGuiElement(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(
                        mosaicTarget.getColorTextureView(),
                        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
                ),
                new Matrix3x2f(guiGraphics.pose()),
                Mth.floor(startPosX),
                Mth.floor(startPosY),
                Mth.ceil(endPosX),
                Mth.ceil(endPosY),
                u0,
                u1,
                v0,
                v1,
                -1,
                scissor
        ));

        return true;
    }

    public static void free()
    {
        capturedThisFrame = false;
        mosaicWidth = -1;
        mosaicHeight = -1;

        if (mosaicTarget != null)
        {
            mosaicTarget.destroyBuffers();
            mosaicTarget = null;
        }
    }

    private static void ensureTargets(int width, int height)
    {
        if (mosaicTarget == null || mosaicWidth != width || mosaicHeight != height)
        {
            if (mosaicTarget != null)
            {
                mosaicTarget.destroyBuffers();
            }

            mosaicTarget = new TextureTarget("spotlight_mosaic_output", width, height, false);
            mosaicWidth = width;
            mosaicHeight = height;
        }
    }

    private static final class MosaicTargetBundle implements PostChain.TargetBundle
    {
        private ResourceHandle<RenderTarget> main;
        private ResourceHandle<RenderTarget> mosaic;

        private MosaicTargetBundle(ResourceHandle<RenderTarget> main, ResourceHandle<RenderTarget> mosaic)
        {
            this.main = main;
            this.mosaic = mosaic;
        }

        @Override
        public void replace(Identifier identifier, @NonNull ResourceHandle<RenderTarget> resourceHandle)
        {
            if (identifier.equals(PostChain.MAIN_TARGET_ID))
            {
                this.main = resourceHandle;
            }
            else if (identifier.equals(MOSAIC_TARGET_ID))
            {
                this.mosaic = resourceHandle;
            }
            else
            {
                throw new IllegalArgumentException("No target with id " + identifier);
            }
        }

        @Override
        public ResourceHandle<RenderTarget> get(Identifier identifier)
        {
            if (identifier.equals(PostChain.MAIN_TARGET_ID))
            {
                return this.main;
            }
            if (identifier.equals(MOSAIC_TARGET_ID))
            {
                return this.mosaic;
            }
            return null;
        }
    }
}