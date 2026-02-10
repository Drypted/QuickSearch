package com.drypted.spotlight.client.styling;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;

public final class Styles
{
    private static final Color BACKGROUND_COLOR = Color.fromInt(0xFF090C08)
                                                       .withHalfAlpha();                  // Onyx
    private static final Color TEXT_COLOR = Color.fromInt(0xFFF7F7FF);           // Ghost White

    private static final Color PRIMARY_COLOR = Color.fromInt(0xFF22007C);     // Navy
    private static final Color SECONDARY_COLOR = Color.fromInt(0xFF0D00A4);   // Navy Electric
    private static final Color TERTIARY_COLOR = Color.fromInt(0xFF2274A5);    // Rich Cerulean
    private static final Color QUATERNARY_COLOR = Color.fromInt(0xFF3681AD);  // Ocean Blue

    private static final Color ERROR_COLOR = Colors.RED;

    public static final class Hotbar
    {
        // hotbar slot
        public static final Color SLOT_CLICKED_COLOR = Styles.SECONDARY_COLOR;
        public static final Color SLOT_FOCUSED_COLOR = Styles.PRIMARY_COLOR;
        public static final Color SLOT_HIGHLIGHTED_COLOR = Styles.TERTIARY_COLOR;
        // hotbar help text
        public static final Color HELP_TEXT_COLOR = Styles.TEXT_COLOR;
        public static final Color HELP_TEXT_CLOSE_BUTTON_COLOR = Styles.ERROR_COLOR;
        // hotbar help text, close button tooltip
        public static final Color TOOLTIP_BACKGROUND_COLOR = Styles.BACKGROUND_COLOR;
        public static final Color TOOLTIP_TEXT_COLOR = Styles.TEXT_COLOR;
        public static final Color TOOLTIP_OUTLINE_COLOR = Styles.TEXT_COLOR;
    }

    public static final class Input
    {
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND_COLOR;
        public static final Color OUTLINE_COLOR = Styles.TEXT_COLOR;
        // text
        public static final Color CARET_COLOR = Styles.TEXT_COLOR;
        public static final Color TEXT_COLOR = Styles.TEXT_COLOR;
        public static final Color DISABLED_TEXT = Colors.WHITE.withHalfAlpha();
        public static final Color PLACEHOLDER_TEXT = Colors.WHITE.withHalfAlpha();
        // selection
        public static final Color SELECTION_BACKGROUND = Color.fromRGBA(147, 197, 253, 255);
        public static final Color SELECTION_TEXT = Color.fromRGBA(11, 11, 11, 255);
        // misc
        public static final Color ERROR_COLOR = Styles.ERROR_COLOR;
        public static final Color LOADER_COLOR = Color.fromRGBA(
                59,
                130,
                246,
                255
        );     // Modern blue
    }

    public static final class ResultData
    {
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND_COLOR;
        public static final Color TEXT_COLOR = Styles.TEXT_COLOR;
        // indicators
        public static final Color HOVER_COLOR = Styles.PRIMARY_COLOR;
        public static final Color CLICK_COLOR = Styles.SECONDARY_COLOR;
        public static final Color SELECTED_COLOR = Styles.TERTIARY_COLOR;
    }

    public static final class ScrollBox
    {
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND_COLOR;
        public static final Color OUTLINE_COLOR = Styles.SECONDARY_COLOR;
        // scrollbar
        public static final Color SCROLLBAR_COLOR = Styles.PRIMARY_COLOR;
        public static final Color SCROLLER_COLOR = Styles.QUATERNARY_COLOR;
    }
}
