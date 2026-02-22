#version 150

uniform sampler2D ScreenTexture;
uniform vec2 ScreenSize;
uniform vec4 Region;       // x, y, w, h in screen pixels
uniform float PixelSize;
uniform vec4 TintColor;

out vec4 fragColor;

void main() {
    // gl_FragCoord is in screen pixels, bottom-left origin
    vec2 fragPos = gl_FragCoord.xy;

    // Snap to mosaic grid (relative to region start)
    vec2 regionStart = vec2(Region.x, ScreenSize.y - Region.y - Region.w); // flip Y
    vec2 localPos = fragPos - regionStart;
    vec2 snapped = floor(localPos / PixelSize) * PixelSize + PixelSize * 0.5;
    vec2 samplePos = regionStart + snapped;

    vec2 uv = samplePos / ScreenSize;
    uv.y = 1.0 - uv.y; // flip Y for texture coords

    uv = clamp(uv, vec2(0.0), vec2(1.0));

    vec4 mosaic = texture(ScreenTexture, uv);

    // Blend tint over mosaic
    vec4 tint = TintColor;
    fragColor = vec4(
    mix(mosaic.rgb, tint.rgb, tint.a),
    1.0
    );
}