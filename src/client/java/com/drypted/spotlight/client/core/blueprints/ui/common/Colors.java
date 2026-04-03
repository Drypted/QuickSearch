package com.drypted.spotlight.client.core.blueprints.ui.common;

public class Colors
{
    public static final Color CLEAR = Color.fromRGBA(0, 0, 0, 0);
    public static final Color BLACK = Color.fromRGBA(0, 0, 0, 255);
    public static final Color WHITE = Color.fromRGBA(255, 255, 255, 255);
    public static final Color RED = Color.fromRGBA(255, 0, 0, 255);
    public static final Color GREEN = Color.fromRGBA(0, 255, 0, 255);
    public static final Color BLUE = Color.fromRGBA(0, 0, 255, 255);
    public static final Color YELLOW = Color.fromRGBA(255, 255, 0, 255);
    public static final Color GRAY = Color.fromRGBA(128, 128, 128, 255);

    // int variants
    public static final int iCLEAR = Colors.CLEAR.asInt();
    public static final int iBLACK = Colors.BLACK.asInt();
    public static final int iWHITE = Colors.WHITE.asInt();
    public static final int iRED = Colors.RED.asInt();
    public static final int iGREEN = Colors.GREEN.asInt();
    public static final int iBLUE = Colors.BLUE.asInt();
    public static final int iYELLOW = Colors.YELLOW.asInt();
    public static final int iGRAY = Colors.GRAY.asInt();

    // Shadow
    public static final Color SHADOW = BLACK.withAlpha(64);

    // More refined colors for UI indicators
    public static final Color SUCCESS_GREEN = Color.fromRGBA(34, 197, 94, 255);     // Modern green
    public static final Color HIGHLIGHT_YELLOW = Color.fromRGBA(234, 179, 8, 255);  // Modern yellow
    public static final Color ERROR_RED = Color.fromRGBA(239, 68, 68, 255);         // Modern red
    public static final Color INFO_BLUE = Color.fromRGBA(59, 130, 246, 255);        // Modern blue

    // Colors for Text Input
    public static final Color SELECTION_BG = Color.fromRGBA(147, 197, 253, 255);   // Light blue
    public static final Color SELECTION_TEXT = Color.fromRGBA(11, 11, 11, 255);    // Very dark gray

    // DEBUG
    public static final Color DEBUG_RECT_OUTLINE = Color.fromRGBA(168, 85, 247, 255);
    // ^ Modern purple
    public static final Color DEBUG_RECT_FILL = DEBUG_RECT_OUTLINE.withAlpha(64);
}
