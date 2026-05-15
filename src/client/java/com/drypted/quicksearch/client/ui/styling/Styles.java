package com.drypted.quicksearch.client.ui.styling;

import com.drypted.quicksearch.client.core.blueprints.ui.common.Color;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Colors;

public final class Styles
{
    private static final Color PRIMARY_COLOR = Color.fromInt(0xFF_0D1B2A);
    //    private static final Color SECONDARY_COLOR = Color.fromInt(0xFF_007CF5);
    //    private static final Color TERTIARY_COLOR = Color.fromInt(0xFF_006ACE);
    //    private static final Color QUATERNARY_COLOR = Color.fromInt(0xFF_6CB6FE);

    private static final Color SECONDARY_COLOR = PRIMARY_COLOR.brighten(0.275f).saturate(0.4f);
    private static final Color TERTIARY_COLOR = SECONDARY_COLOR.brighten(0.15f).saturate(0.4f);
    private static final Color QUATERNARY_COLOR = TERTIARY_COLOR.brighten(0.075f).saturate(0.4f);

    private static final Color INFO_COLOR = Color.fromInt(0xFF_0084D1);
    private static final Color SUCCESS_COLOR = Color.fromInt(0xFF_00C853);
    private static final Color WARNING_COLOR = Color.fromInt(0xFF_FFBA00);
    private static final Color ERROR_COLOR = Colors.RED;

    private static final Color BACKGROUND = PRIMARY_COLOR.withAlpha(128);
    private static final Color OUTLINE = PRIMARY_COLOR;
    private static final Color TEXT = PRIMARY_COLOR.getReadableTextColor(4.5f);
    private static final Color DISABLED_TEXT = PRIMARY_COLOR.getReadableTextColor(3.0f).withHalfOpacity();

    public static final float THICK = 2.f;
    public static final float THIN = 1.f;

    public static final class Hotbar
    {
        public static final float OUTLINE_THICKNESS = Styles.THIN;
        public static final float HELP_TEXT_OUTLINE_THICKNESS = Styles.THIN;
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
        public static final float OUTLINE_THICKNESS = Styles.THICK;
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color OUTLINE_COLOR = Styles.OUTLINE;
        // text
        public static final Color CARET_COLOR = Styles.TEXT;
        public static final Color TEXT_COLOR = Styles.TEXT;
        public static final Color DISABLED_TEXT = Styles.DISABLED_TEXT;
        public static final Color PLACEHOLDER_TEXT = Styles.DISABLED_TEXT;
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
        public static final float OUTLINE_THICKNESS = Styles.THIN;
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND.withAlpha(128);
        public static final Color TEXT_COLOR = Styles.TEXT;
        // indicators
        public static final Color SELECTED_OUTLINE_COLOR = Styles.TERTIARY_COLOR;
        public static final Color CLICKED_OUTLINE_COLOR = Styles.SECONDARY_COLOR;
        public static final Color HOVER_OUTLINE_COLOR = Styles.QUATERNARY_COLOR;
    }

    public static final class ScrollBox
    {
        public static final float OUTLINE_THICKNESS = Styles.THICK;
        // box
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color OUTLINE_COLOR = Styles.OUTLINE;
        // scrollbar
        public static final Color SCROLLBAR_COLOR = Styles.BACKGROUND;
        public static final Color SCROLLER_COLOR = Styles.OUTLINE;
    }
}
