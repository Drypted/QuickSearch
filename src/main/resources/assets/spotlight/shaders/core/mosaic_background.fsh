#version 150

uniform sampler2D InSampler;

out vec4 fragColor;

void main() {
    vec2 textureSizePixels = vec2(textureSize(InSampler, 0));
    float pixelSize = 16.0;

    vec2 snapped = floor(gl_FragCoord.xy / pixelSize) * pixelSize + pixelSize * 0.5;
    vec2 uv = clamp(snapped / textureSizePixels, vec2(0.0), vec2(1.0));

    fragColor = texture(InSampler, uv);
}