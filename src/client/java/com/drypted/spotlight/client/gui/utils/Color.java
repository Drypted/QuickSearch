package com.drypted.spotlight.client.gui.utils;

/**
 * Immutable color representation with RGBA components stored as a 32-bit integer.
 *
 * <p>Color format: 0xAARRGGBB (Alpha-Red-Green-Blue)</p>
 * <ul>
 * <li>Bits 24-31: Alpha channel (0-255, where 0 is transparent and 255 is opaque)</li>
 * <li>Bits 16-23: Red channel (0-255)</li>
 * <li>Bits 8-15: Green channel (0-255)</li>
 * <li>Bits 0-7: Blue channel (0-255)</li>
 * </ul>
 *
 * <p>This class provides methods for color manipulation including:</p>
 * <ul>
 * <li>RGB/HSL color space conversions</li>
 * <li>Component-wise color adjustments</li>
 * <li>Alpha blending and opacity modifications</li>
 * <li>Color mixing and interpolation</li>
 * </ul>
 *
 * @author Drypted Spotlight
 * @version 2.0
 */
public class Color
{
    /**
     * Internal RGBA color representation as 32-bit integer (0xAARRGGBB)
     */
    private int color;

    /* CONSTRUCTORS */

    /**
     * Constructs a color from a 32-bit RGBA integer.
     *
     * @param rgba the color value in 0xAARRGGBB format
     */
    public Color(int rgba)
    {
        this.color = rgba;
    }

    /**
     * Constructs an opaque color from RGB components. Alpha is set to 255 (fully opaque).
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     */
    public Color(int r, int g, int b)
    {
        this.color = fromRGBA(r, g, b, 255).asInt();
    }

    /**
     * Constructs a color from RGBA components.
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @param a alpha component (0-255, where 0 is transparent and 255 is opaque)
     */
    public Color(int r, int g, int b, int a)
    {
        this.color = fromRGBA(r, g, b, a).asInt();
    }


    /* FACTORY METHODS */

    /**
     * Creates a color from individual RGBA components. Uses bit-shifting to pack components into a 32-bit integer.
     *
     * <p>Algorithm: ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF)</p>
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @param a alpha component (0-255)
     *
     * @return a new Color instance
     */
    public static Color fromRGBA(int r, int g, int b, int a)
    {
        return new Color(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    /**
     * Creates a color from a 32-bit integer representation.
     *
     * @param aarrggbb the color value in 0xAARRGGBB format
     *
     * @return a new Color instance
     */
    public static Color fromInt(int aarrggbb)
    {
        return new Color(aarrggbb);
    }

    /**
     * Creates a color from HSL (Hue, Saturation, Lightness) components. Uses the HSL to RGB conversion algorithm.
     *
     * @param h     hue (0.0-1.0, represents 0-360 degrees)
     * @param s     saturation (0.0-1.0, where 0 is gray and 1 is full color)
     * @param l     lightness (0.0-1.0, where 0 is black and 1 is white)
     * @param alpha alpha component (0.0-1.0, where 0 is transparent and 1 is opaque)
     *
     * @return a new Color instance
     *
     * @see #toHSL()
     */
    public static Color fromHSL(float h, float s, float l, float alpha)
    {
        float r, g, b;

        if (s == 0f)
        {
            // Achromatic (gray) - no saturation means all channels equal
            r = g = b = l;
        }
        else
        {
            // Chromatic color - use HSL to RGB conversion formula
            float q = l < 0.5f ? l * (1f + s) : l + s - l * s;

            float p = 2f * l - q;

            r = hueToRGB(p, q, h + 1f / 3f);
            g = hueToRGB(p, q, h);
            b = hueToRGB(p, q, h - 1f / 3f);
        }

        return fromRGBA((int) (r * 255f), (int) (g * 255f), (int) (b * 255f), (int) (alpha * 255f));
    }


    /* CONVERSIONS */

    /**
     * Returns the color as a 32-bit integer in 0xAARRGGBB format.
     *
     * @return the integer color value
     */
    public int asInt()
    {
        return this.color;
    }

    /**
     * Converts this color to a hexadecimal string.
     *
     * @param includeAlpha whether to include the alpha channel
     *
     * @return hexadecimal string (e.g., "#RRGGBB" or "#AARRGGBB")
     */
    public String toHex(boolean includeAlpha)
    {
        if (includeAlpha)
        {
            return String.format("#%08X", this.color);
        }
        else
        {
            return String.format("#%06X", this.color & 0xFFFFFF);
        }
    }

    /**
     * Converts this color to a hexadecimal string without alpha.
     *
     * @return hexadecimal string (e.g., "#RRGGBB")
     */
    public String toHex()
    {
        return toHex(false);
    }

    /* MUTABLE OPS */

    public void makeOpaque()
    {
        this.color = (this.color & 0x00FFFFFF) | 0xFF000000;
    }

    /* IMMUTABLE OPS */

    /**
     * Returns a new color with the specified alpha value. Original color remains unchanged.
     *
     * @param alpha new alpha value (0-255)
     *
     * @return a new Color instance
     */
    public Color withOpacity(int alpha)
    {
        return fromRGBA(this.getRed(), this.getGreen(), this.getBlue(), alpha);
    }

    /**
     * Returns a new color with half the current opacity. Original color remains unchanged.
     *
     * @return a new Color instance with alpha / 2
     */
    public Color withHalfOpacity()
    {
        return this.withOpacity(this.getAlpha() / 2);
    }

    /**
     * Returns a new color with the specified red component. Original color remains unchanged.
     *
     * @param red new red value (0-255)
     *
     * @return a new Color instance
     */
    public Color withRed(int red)
    {
        return fromRGBA(red, this.getGreen(), this.getBlue(), this.getAlpha());
    }

    /**
     * Returns a new color with the specified green component. Original color remains unchanged.
     *
     * @param green new green value (0-255)
     *
     * @return a new Color instance
     */
    public Color withGreen(int green)
    {
        return fromRGBA(this.getRed(), green, this.getBlue(), this.getAlpha());
    }

    /**
     * Returns a new color with the specified blue component. Original color remains unchanged.
     *
     * @param blue new blue value (0-255)
     *
     * @return a new Color instance
     */
    public Color withBlue(int blue)
    {
        return fromRGBA(this.getRed(), this.getGreen(), blue, this.getAlpha());
    }

    /**
     * Returns a new color with the specified hue. Preserves saturation, lightness, and alpha. Original color remains
     * unchanged.
     *
     * @param hue new hue value (0.0-1.0, represents 0-360 degrees)
     *
     * @return a new Color instance
     */
    public Color withHue(float hue)
    {
        float[] hsl = toHSL();
        hsl[0] = hue;
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns a new color with the specified saturation. Preserves hue, lightness, and alpha. Original color remains
     * unchanged.
     *
     * @param saturation new saturation value (0.0-1.0)
     *
     * @return a new Color instance
     */
    public Color withSaturation(float saturation)
    {
        float[] hsl = toHSL();
        hsl[1] = saturation;
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns a new color with the specified lightness. Preserves hue, saturation, and alpha. Original color remains
     * unchanged.
     *
     * @param lightness new lightness value (0.0-1.0)
     *
     * @return a new Color instance
     */
    public Color withLightness(float lightness)
    {
        float[] hsl = toHSL();
        hsl[2] = lightness;
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }


    /* COLOR OPERATIONS */

    /**
     * Returns a brightened version of this color. Increases lightness by the specified amount (clamped to 0.0-1.0).
     *
     * @param amount amount to brighten (0.0-1.0)
     *
     * @return a new Color instance
     */
    public Color brighten(float amount)
    {
        float[] hsl = toHSL();
        hsl[2] = Math.min(1.0f, hsl[2] + amount);
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns a darkened version of this color. Decreases lightness by the specified amount (clamped to 0.0-1.0).
     *
     * @param amount amount to darken (0.0-1.0)
     *
     * @return a new Color instance
     */
    public Color darken(float amount)
    {
        float[] hsl = toHSL();
        hsl[2] = Math.max(0.0f, hsl[2] - amount);
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns a more saturated version of this color. Increases saturation by the specified amount (clamped to
     * 0.0-1.0).
     *
     * @param amount amount to increase saturation (0.0-1.0)
     *
     * @return a new Color instance
     */
    public Color saturate(float amount)
    {
        float[] hsl = toHSL();
        hsl[1] = Math.min(1.0f, hsl[1] + amount);
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns a desaturated version of this color. Decreases saturation by the specified amount (clamped to 0.0-1.0).
     *
     * @param amount amount to decrease saturation (0.0-1.0)
     *
     * @return a new Color instance
     */
    public Color desaturate(float amount)
    {
        float[] hsl = toHSL();
        hsl[1] = Math.max(0.0f, hsl[1] - amount);
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns a grayscale version of this color. Sets saturation to 0 while preserving lightness and alpha.
     *
     * @return a new Color instance
     */
    public Color grayscale()
    {
        return this.withSaturation(0f);
    }

    /**
     * Returns the complementary color (opposite on the color wheel). Rotates hue by 180 degrees (0.5 in normalized
     * space).
     *
     * @return a new Color instance
     */
    public Color complement()
    {
        float[] hsl = toHSL();
        hsl[0] = (hsl[0] + 0.5f) % 1.0f;
        return fromHSL(hsl[0], hsl[1], hsl[2], this.getAlpha() / 255f);
    }

    /**
     * Returns the inverted color (RGB inversion). Each channel is calculated as 255 - current_value. Alpha is
     * preserved.
     *
     * @return a new Color instance
     */
    public Color invert()
    {
        return fromRGBA(255 - this.getRed(), 255 - this.getGreen(), 255 - this.getBlue(), this.getAlpha());
    }

    /**
     * Linearly interpolates between this color and another. Uses linear interpolation (LERP) formula: a + (b - a) * t
     *
     * @param other the target color
     * @param t     interpolation factor (0.0 = this color, 1.0 = other color)
     *
     * @return a new Color instance
     */
    public Color lerp(Color other, float t)
    {
        t = Math.max(0.0f, Math.min(1.0f, t)); // Clamp t to [0, 1]

        int r = (int) (this.getRed() + (other.getRed() - this.getRed()) * t);
        int g = (int) (this.getGreen() + (other.getGreen() - this.getGreen()) * t);
        int b = (int) (this.getBlue() + (other.getBlue() - this.getBlue()) * t);
        int a = (int) (this.getAlpha() + (other.getAlpha() - this.getAlpha()) * t);

        return fromRGBA(r, g, b, a);
    }

    /**
     * Mixes this color with another using equal weights. Equivalent to lerp(other, 0.5).
     *
     * @param other the color to mix with
     *
     * @return a new Color instance
     */
    public Color mix(Color other)
    {
        return this.lerp(other, 0.5f);
    }

    /**
     * Blends this color over another using alpha compositing. Uses the "over" operator from Porter-Duff compositing.
     *
     * <p>Formula: C_out = C_src * α_src + C_dst * (1 - α_src)</p>
     *
     * @param background the background color
     *
     * @return a new Color instance representing the composited result
     */
    public Color blendOver(Color background)
    {
        float srcAlpha = this.getAlpha() / 255f;
        float dstAlpha = background.getAlpha() / 255f;

        float outAlpha = srcAlpha + dstAlpha * (1f - srcAlpha);

        if (outAlpha == 0f)
        {
            return new Color(0, 0, 0, 0);
        }

        int r = (int) ((this.getRed() * srcAlpha + background.getRed() * dstAlpha * (1f - srcAlpha)) / outAlpha);
        int g = (int) ((this.getGreen() * srcAlpha + background.getGreen() * dstAlpha * (1f - srcAlpha)) / outAlpha);
        int b = (int) ((this.getBlue() * srcAlpha + background.getBlue() * dstAlpha * (1f - srcAlpha)) / outAlpha);
        int a = (int) (outAlpha * 255f);

        return fromRGBA(r, g, b, a);
    }


    /* COMPONENT GETTERS */

    /**
     * Extracts the red component using bit-shifting and masking.
     *
     * <p>Algorithm: (color >> 16) & 0xFF</p>
     *
     * @return red component (0-255)
     */
    public int getRed()
    {
        return (this.color >> 16) & 0xFF;
    }

    /**
     * Extracts the green component using bit-shifting and masking.
     *
     * <p>Algorithm: (color >> 8) & 0xFF</p>
     *
     * @return green component (0-255)
     */
    public int getGreen()
    {
        return (this.color >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component using bit masking.
     *
     * <p>Algorithm: color & 0xFF</p>
     *
     * @return blue component (0-255)
     */
    public int getBlue()
    {
        return this.color & 0xFF;
    }

    /**
     * Extracts the alpha component using bit-shifting and masking.
     *
     * <p>Algorithm: (color >> 24) & 0xFF</p>
     *
     * @return alpha component (0-255)
     */
    public int getAlpha()
    {
        return (this.color >> 24) & 0xFF;
    }

    /**
     * Returns the hue component in HSL color space.
     *
     * @return hue (0.0-1.0, represents 0-360 degrees)
     */
    public float getHue()
    {
        return toHSL()[0];
    }

    /**
     * Returns the saturation component in HSL color space.
     *
     * @return saturation (0.0-1.0)
     */
    public float getSaturation()
    {
        return toHSL()[1];
    }

    /**
     * Returns the lightness component in HSL color space.
     *
     * @return lightness (0.0-1.0)
     */
    public float getLightness()
    {
        return toHSL()[2];
    }

    /**
     * Calculates the perceived brightness using the relative luminance formula. Uses ITU-R BT.709 coefficients.
     *
     * <p>Formula: 0.2126 * R + 0.7152 * G + 0.0722 * B</p>
     *
     * @return perceived brightness (0.0-1.0)
     */
    public float getBrightness()
    {
        return (0.2126f * getRed() + 0.7152f * getGreen() + 0.0722f * getBlue()) / 255f;
    }


    /* HSL CONVERSION */

    /**
     * Converts RGB to HSL color space using the standard algorithm.
     *
     * <p><b>Algorithm (RGB to HSL):</b></p>
     * <ol>
     * <li>Normalize RGB values to [0, 1] range</li>
     * <li>Find max and min of R, G, B</li>
     * <li>Calculate Lightness: L = (max + min) / 2</li>
     * <li>If max == min: achromatic (H = 0, S = 0)</li>
     * <li>Otherwise:
     *  <ul>
     *  <li>Calculate Saturation: S = (max - min) / (L > 0.5 ? 2 - max - min : max + min)</li>
     *  <li>Calculate Hue based on which channel is max:
     *   <ul>
     *   <li>If R is max: H = (G - B) / delta + (G < B ? 6 : 0)</li>
     *   <li>If G is max: H = (B - R) / delta + 2</li>
     *   <li>If B is max: H = (R - G) / delta + 4</li>
     *   </ul>
     *  </li>
     *  <li>Normalize Hue: H = H / 6 to get [0, 1] range</li>
     *  </ul>
     * </li>
     * </ol>
     *
     * @return float array [hue, saturation, lightness] where each value is in range [0.0, 1.0]
     */
    private float[] toHSL()
    {
        // Step 1: Normalize RGB to [0, 1]
        float r = this.getRed() / 255f;
        float g = this.getGreen() / 255f;
        float b = this.getBlue() / 255f;

        // Step 2: Find max and min
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        float h, s, l;

        // Step 3: Calculate Lightness
        l = (max + min) / 2f;

        // Step 4: Check if achromatic
        if (max == min)
        {
            h = 0f; // Undefined, conventionally set to 0
            s = 0f; // No saturation for gray
        }
        else
        {
            // Step 5: Calculate chromaticity
            float d = max - min;

            // Calculate Saturation
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);

            // Calculate Hue
            if (max == r)
            {
                h = (g - b) / d + (g < b ? 6f : 0f);
            }
            else if (max == g)
            {
                h = (b - r) / d + 2f;
            }
            else // max == b
            {
                h = (r - g) / d + 4f;
            }

            // Normalize hue to [0, 1]
            h /= 6f;
        }

        return new float[]{h, s, l};
    }

    /**
     * Helper function for HSL to RGB conversion. Converts a hue value and intermediate p, q values to an RGB
     * component.
     *
     * <p><b>Algorithm (HSL to RGB helper):</b></p>
     * <ul>
     * <li>Wraps hue to [0, 1] range</li>
     * <li>If t < 1/6: linear interpolation in first segment</li>
     * <li>If t < 1/2: constant value at peak</li>
     * <li>If t < 2/3: linear interpolation in second segment</li>
     * <li>Otherwise: constant value at base</li>
     * </ul>
     *
     * @param p the base color component value
     * @param q the peak color component value
     * @param t the normalized hue offset
     *
     * @return the RGB component value (0.0-1.0)
     */
    private static float hueToRGB(float p, float q, float t)
    {
        // Wrap hue to [0, 1] range
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;

        // Piecewise linear function for hue
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;

        return p;
    }

    /* TEXT */

    /**
     * Returns either black or white text depending on which provides better contrast against this color.
     *
     * @param minContrast minimum WCAG contrast ratio (e.g. 4.5 for normal text, 3.0 for disabled/large text, 7.0 for
     *                    high contrast)
     *
     * @return a new Color instance (black or white)
     */
    public Color getReadableTextColor(float minContrast)
    {
        Color black = new Color(0, 0, 0, 255);
        Color white = new Color(255, 255, 255, 255);

        float contrastWithBlack = contrastRatio(this, black);
        float contrastWithWhite = contrastRatio(this, white);

        boolean blackValid = contrastWithBlack >= minContrast;
        boolean whiteValid = contrastWithWhite >= minContrast;

        if (blackValid && !whiteValid) return black;
        if (whiteValid && !blackValid) return white;

        // If both valid or both invalid, return higher contrast
        return contrastWithBlack > contrastWithWhite ? black : white;
    }

    /**
     * Computes WCAG contrast ratio between two colors. Formula: (L1 + 0.05) / (L2 + 0.05)
     */
    private static float contrastRatio(Color c1, Color c2)
    {
        float l1 = relativeLuminance(c1);
        float l2 = relativeLuminance(c2);

        float lighter = Math.max(l1, l2);
        float darker = Math.min(l1, l2);

        return (lighter + 0.05f) / (darker + 0.05f);
    }

    /**
     * Computes WCAG relative luminance using sRGB gamma correction.
     */
    private static float relativeLuminance(Color c)
    {
        float r = linearize(c.getRed() / 255f);
        float g = linearize(c.getGreen() / 255f);
        float b = linearize(c.getBlue() / 255f);

        return 0.2126f * r + 0.7152f * g + 0.0722f * b;
    }

    /**
     * Converts sRGB to linear RGB.
     */
    private static float linearize(float channel)
    {
        return channel <= 0.04045f ? channel / 12.92f : (float) Math.pow((channel + 0.055f) / 1.055f, 2.4f);
    }


    /* OBJECT METHODS */

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Color other = (Color) obj;
        return this.color == other.color;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(this.color);
    }

    @Override
    public String toString()
    {
        return toHex();
    }
}
