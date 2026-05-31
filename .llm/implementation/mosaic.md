# Mosaic Implementation

> **Status (last updated 2026-05-31): DISABLED for now.**
> The mosaic background feature is currently turned off. The integration code
> described below is not wired into the live render path: there is no
> `MosaicBackgroundRenderer` class in `src/`, and `RenderCommon` does not call
> any mosaic blit. What still exists in the tree:
> - `src/main/resources/assets/quicksearch/post_effect/mosaic_background.json`
> - `src/main/resources/assets/quicksearch/shaders/core/mosaic_background.fsh`
>   (both still reference the legacy `spotlight:` namespace IDs internally)
> - the `GameRendererAccessor` mixin (`com.drypted.quicksearch.client.mixin`)
>
> This document is retained as the design/reference for re-enabling the feature.
> Paths below predate the `spotlight` -> `quicksearch` rename; treat class/asset
> locations as `com.drypted.quicksearch.*` / `assets/quicksearch/*`.

## Overview

The current mosaic background implementation uses a two-stage pipeline:

1. A post-effect pass renders a full-screen pixelated copy of the current main render target into a dedicated offscreen `TextureTarget`.
2. Widget background rendering blits only the relevant rectangle from that precomputed mosaic target, then overlays the normal translucent widget tint and outline.

This keeps the effect scoped to widget backgrounds instead of applying it to the full screen.

## Runtime Flow

### 1. Screen capture trigger

`SpotlightScreen.render(...)` is expected to call:

```java
MosaicBackgroundRenderer.captureFramebuffer();
```

This should happen before the UI widgets render, so the mosaic target contains the world/background scene rather than the widgets themselves.

### 2. Mosaic target generation

`MosaicBackgroundRenderer.captureFramebuffer()`:

- gets the main render target from `Minecraft.getInstance().getMainRenderTarget()`
- ensures `mosaicTarget` matches the current framebuffer size
- loads the custom post chain with id `spotlight:mosaic_background`
- executes that post chain with two external targets:
  - `minecraft:main`
  - `spotlight:mosaic_output`
- stores the result in `mosaicTarget`
- marks `capturedThisFrame = true`

The post chain execution uses the game renderer resource pool through the `GameRendererAccessor` mixin.

## Rendering Path

### Widget background usage

`RenderCommon.drawRectangle(...)` calls:

```java
MosaicBackgroundRenderer.drawMosaic(g, x0, y0, x1, y1)
```

for the body of the rectangle.

`drawMosaic(...)`:

- checks that the mosaic target was captured this frame
- converts GUI coordinates into normalized UVs
- flips V coordinates because render-target texture sampling is upside down relative to GUI top-left coordinates
- submits a `BlitRenderState` using `RenderPipelines.GUI_TEXTURED`
- samples from `mosaicTarget.getColorTextureView()` with a nearest-neighbor sampler

After the mosaic blit, `RenderCommon.drawRectangle(...)` draws the normal translucent fill quad and then the outline quads.

## Shader Resources

### Post-effect config

File:

`src/main/resources/assets/spotlight/post_effect/mosaic_background.json`

This defines a single pass:

- vertex shader: `minecraft:core/screenquad`
- fragment shader: `spotlight:core/mosaic_background`
- input sampler: `minecraft:main`
- output target: `spotlight:mosaic_output`

### Fragment shader

File:

`src/main/resources/assets/spotlight/shaders/core/mosaic_background.fsh`

Current behavior:

- reads from `InSampler`
- computes a fixed pixel size of `16.0`
- snaps `gl_FragCoord.xy` to the center of a mosaic cell
- converts the snapped position back to UV space
- samples the source texture once at that snapped UV

This produces a pixelated fullscreen copy in the mosaic output target.

## Core Classes

### `MosaicBackgroundRenderer`

Responsibilities:

- allocate and free `mosaicTarget`
- run the mosaic post chain each frame
- blit cropped widget regions from the mosaic target
- translate between GUI rectangle coordinates and target UVs

### `GameRendererAccessor`

Mixin accessor used to read `GameRenderer.resourcePool`.

Current accessor return type must match the concrete field type exactly:

```java
CrossFrameResourcePool spotlight$getResourcePool();
```

Using `GraphicsResourceAllocator` directly as the accessor return type causes a mixin startup failure because accessor matching is signature-based.

### `RenderCommon`

Owns rectangle drawing. The mosaic implementation is currently integrated here so every rectangle body can use the precomputed mosaic background.

## Important Constraints

- The post-effect must run before widget rendering if the mosaic should only show the underlying scene.
- The mosaic target must track framebuffer size changes.
- Render-target UV sampling requires vertical flipping for GUI blits.
- The current shader uses a fixed mosaic size (`16.0`), not a configurable one.
- The current rectangle implementation always draws the translucent body quad after the mosaic blit.

## Current File References

- `src/client/java/com/drypted/spotlight/client/ui/renderer/MosaicBackgroundRenderer.java`
- `src/client/java/com/drypted/spotlight/client/ui/renderer/RenderCommon.java`
- `src/client/java/com/drypted/spotlight/client/mixin/GameRendererAccessor.java`
- `src/main/resources/assets/spotlight/post_effect/mosaic_background.json`
- `src/main/resources/assets/spotlight/shaders/core/mosaic_background.fsh`
