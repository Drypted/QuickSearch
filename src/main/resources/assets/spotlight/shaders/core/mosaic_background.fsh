#version 150

uniform sampler2D ScreenTexture;
uniform vec2 ScreenSize;
uniform vec4 Region;
uniform float PixelSize;
uniform vec4 TintColor;

out vec4 fragColor;

void main() {
    // gl_FragCoord is bottom-left origin
    vec2 fragPos = gl_FragCoord.xy;

    // Snap in screen space (bottom-left origin)
    vec2 snapped = floor(fragPos / PixelSize) * PixelSize + PixelSize * 0.5;

    // Convert to UV
    vec2 uv = snapped / ScreenSize;
    uv = clamp(uv, vec2(0.0), vec2(1.0));

    vec4 mosaic = texture(ScreenTexture, uv);

    // Blend tint over mosaic
    vec4 tint = TintColor;
    fragColor = vec4(mix(mosaic.rgb, tint.rgb, tint.a), 1.0);
}