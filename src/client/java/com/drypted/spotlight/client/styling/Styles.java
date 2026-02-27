package com.drypted.spotlight.client.styling;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;

public final class Styles
{
    private static final Color PRIMARY_COLOR = Color.fromInt(0xFF_1C90FF);
    private static final Color SECONDARY_COLOR = Color.fromInt(0xFF_007CF5);
    private static final Color TERTIARY_COLOR = Color.fromInt(0xFF_006ACE);

    private static final Color QUATERNARY_COLOR = Color.fromInt(0xFF_6CB6FE);

    /**
     * #0D1B2A ink black #0D0221 midnight violet #150578 navy #0E0E52 deep twilight #ED1C24 racing red
     */

    private static final Color INFO_COLOR = Color.fromInt(0xFF_0084D1);
    private static final Color SUCCESS_COLOR = Color.fromInt(0xFF_00C853);
    private static final Color WARNING_COLOR = Color.fromInt(0xFF_FFBA00);
    private static final Color ERROR_COLOR = Colors.RED;

    private static final Color BACKGROUND = Color.fromInt(0xAA_0D1B2A);
    private static final Color OUTLINE = Color.fromInt(0xFF_0D1B2A);
    private static final Color TEXT = Color.fromInt(0xFF_FFFFFF);

    public static final class Hotbar
    {
        public static final int OUTLINE_THICKNESS = 1;
        public static final int HELP_TEXT_OUTLINE_THICKNESS = 1;
        // hotbar slot outline
        public static final Color FOCUSED_COLOR = Styles.OUTLINE;
        public static final Color SLOT_PRESSED_COLOR = Styles.SECONDARY_COLOR;
        public static final Color SLOT_HIGHLIGHTED_COLOR = Styles.TERTIARY_COLOR;
        // hotbar help text
        public static final Color HELP_TEXT_COLOR = Styles.TEXT;
        public static final Color HELP_TEXT_CLOSE_BUTTON_COLOR = Styles.ERROR_COLOR;
        // hotbar help text, close button tooltip
        public static final Color TOOLTIP_BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color TOOLTIP_TEXT_COLOR = Styles.TEXT;
        public static final Color TOOLTIP_OUTLINE_COLOR = Styles.OUTLINE;
    }

    public static final class Input
    {
        public static final int OUTLINE_THICKNESS = 2;
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color OUTLINE_COLOR = Styles.OUTLINE;
        // text
        public static final Color CARET_COLOR = Styles.TEXT;
        public static final Color TEXT_COLOR = Styles.TEXT;
        public static final Color DISABLED_TEXT = Colors.GRAY;
        public static final Color PLACEHOLDER_TEXT = Colors.GRAY;
        // selection
        public static final Color SELECTION_BACKGROUND = Color.fromInt(0xFF_93C5FD);
        public static final Color SELECTION_TEXT = Color.fromInt(0xFF_1E1E1E);
        // misc
        public static final Color INFO_COLOR = Styles.INFO_COLOR;
        public static final Color SUCCESS_COLOR = Styles.SUCCESS_COLOR;
        public static final Color WARNING_COLOR = Styles.WARNING_COLOR;
        public static final Color ERROR_COLOR = Styles.ERROR_COLOR;
        public static final Color LOADER_COLOR = Color.fromInt(0xFF_3B82F6);
    }

    public static final class ResultData
    {
        public static final int OUTLINE_THICKNESS = 1;
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND.withOpacity(128);
        public static final Color TEXT_COLOR = Styles.TEXT;
        // indicators
        public static final Color SELECTED_OUTLINE_COLOR = Styles.PRIMARY_COLOR;
        public static final Color CLICKED_OUTLINE_COLOR = Styles.SECONDARY_COLOR;
        public static final Color HOVER_OUTLINE_COLOR = Styles.QUATERNARY_COLOR;
    }

    public static final class ScrollBox
    {
        public static final int OUTLINE_THICKNESS = 2;
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color OUTLINE_COLOR = Styles.OUTLINE;
        // scrollbar
        public static final Color SCROLLBAR_COLOR = Colors.CLEAR;
        public static final Color SCROLLER_COLOR = Styles.OUTLINE;
    }
}
