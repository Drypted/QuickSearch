package com.drypted.spotlight.client.styling;

import com.drypted.spotlight.client.gui.utils.Color;
import com.drypted.spotlight.client.gui.utils.Colors;

public final class Styles
{
    private static final Color THEME_BLACK = Color.fromInt(0xFF000000).withHalfOpacity();
    private static final Color THEME_WHITE = Color.fromInt(0xFFFFFFFF);

    private static final Color PRIMARY_COLOR = Color.fromInt(0xFF1C90FF);
    private static final Color SECONDARY_COLOR = Color.fromInt(0xFF007CF5);
    private static final Color TERTIARY_COLOR = Color.fromInt(0xFF006ACE);

    private static final Color QUATERNARY_COLOR = Color.fromInt(0xFF6CB6FE);

    private static final Color ERROR_COLOR = Colors.RED;

    public static final class Hotbar
    {
        // hotbar slot outline
        public static final Color FOCUSED_COLOR = Styles.QUATERNARY_COLOR;
        public static final Color SLOT_PRESSED_COLOR = Styles.SECONDARY_COLOR;
        public static final Color SLOT_HIGHLIGHTED_COLOR = Styles.TERTIARY_COLOR;
        // hotbar help text
        public static final Color HELP_TEXT_COLOR = Styles.THEME_WHITE;
        public static final Color HELP_TEXT_CLOSE_BUTTON_COLOR = Styles.ERROR_COLOR;
        // hotbar help text, close button tooltip
        public static final Color TOOLTIP_BACKGROUND_COLOR = Styles.THEME_BLACK;
        public static final Color TOOLTIP_TEXT_COLOR = Styles.THEME_WHITE;
        public static final Color TOOLTIP_OUTLINE_COLOR = Styles.THEME_WHITE;
    }

    public static final class Input
    {
        // box
        public static final Color BACKGROUND_COLOR = Styles.THEME_BLACK;
        public static final Color OUTLINE_COLOR = Styles.THEME_WHITE;
        // text
        public static final Color CARET_COLOR = Styles.THEME_WHITE;
        public static final Color TEXT_COLOR = Styles.THEME_WHITE;
        public static final Color DISABLED_TEXT = Colors.GRAY;
        public static final Color PLACEHOLDER_TEXT = Colors.GRAY;
        // selection
        public static final Color SELECTION_BACKGROUND = Color.fromInt(0xFF93C5FD);
        public static final Color SELECTION_TEXT = Color.fromInt(0xFF1E1E1E);
        // misc
        public static final Color ERROR_COLOR = Styles.ERROR_COLOR;
        public static final Color LOADER_COLOR = Color.fromInt(0xFF3B82F6); // Modern blue
    }

    public static final class ResultData
    {
        // box
        public static final Color BACKGROUND_COLOR = Styles.THEME_BLACK;
        public static final Color TEXT_COLOR = Styles.THEME_WHITE;
        // indicators
        public static final Color SELECTED_OUTLINE_COLOR = Styles.PRIMARY_COLOR;
        public static final Color CLICKED_OUTLINE_COLOR = Styles.SECONDARY_COLOR;
        public static final Color HOVER_OUTLINE_COLOR = Styles.QUATERNARY_COLOR;
    }

    public static final class ScrollBox
    {
        // box
        public static final Color BACKGROUND_COLOR = Styles.THEME_BLACK;
        public static final Color OUTLINE_COLOR = Styles.THEME_WHITE;
        // scrollbar
        public static final Color SCROLLBAR_COLOR = Styles.THEME_WHITE;
        public static final Color SCROLLER_COLOR = Styles.THEME_WHITE;
    }
}
