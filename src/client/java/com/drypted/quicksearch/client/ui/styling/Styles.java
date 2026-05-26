package com.drypted.quicksearch.client.ui.styling;

import com.drypted.quicksearch.client.core.blueprints.ui.common.Color;
import com.drypted.quicksearch.client.core.blueprints.ui.common.Colors;
import com.drypted.quicksearch.client.core.blueprints.ui.common.RoundedCorners;

public final class Styles
{
    // semantic
    private static final Color INFO_COLOR = Color.fromInt(0xFF_0084D1);
    private static final Color SUCCESS_COLOR = Color.fromInt(0xFF_00C853);
    private static final Color WARNING_COLOR = Color.fromInt(0xFF_FFBA00);
    private static final Color ERROR_COLOR = Colors.RED;

    // common
    private static final Color BACKGROUND = Colors.BLACK.withAlpha(128);
    private static final Color OUTLINE = Colors.WHITE;
    private static final Color TEXT = BACKGROUND.getReadableTextColor(4.5f);
    private static final Color DISABLED_TEXT = BACKGROUND.getReadableTextColor(3.0f).withHalfOpacity();

    // thickness
    private static final float THICK = 2.f;
    private static final float THIN = 1.f;

    public static final class Hotbar
    {
        // outline
        public static final float OUTLINE_THICKNESS = Styles.THIN;
        public static final float HELP_TOOLTIP_OUTLINE_THICKNESS = Styles.THIN;
        // slot key-label colors
        public static final Color SLOT_LABEL_BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color SLOT_LABEL_TEXT_COLOR = Styles.BACKGROUND.getReadableTextColor(7f);
        public static final Color SLOT_LABEL_OUTLINE_COLOR = Styles.OUTLINE;
        // slot state colors
        public static final Color SLOT_FOCUSED_COLOR = Styles.BACKGROUND;
        public static final Color SLOT_PRESSED_COLOR = ResultData.CLICKED_BACKGROUND_COLOR;
        public static final Color SLOT_HIGHLIGHTED_COLOR = ResultData.SELECTED_BACKGROUND_COLOR;
        // help tooltip state colors
        public static final Color HELP_TOOLTIP_FOCUSED_COLOR = SLOT_FOCUSED_COLOR;
        public static final Color HELP_TOOLTIP_HIGHLIGHTED_COLOR = SLOT_HIGHLIGHTED_COLOR;
        // help tooltip colors
        public static final Color HELP_TOOLTIP_OUTLINE_COLOR = Styles.OUTLINE;
        public static final Color HELP_TOOLTIP_TEXT_COLOR = Styles.BACKGROUND.getReadableTextColor(7f);
        public static final Color HELP_TOOLTIP_CLOSE_BUTTON_COLOR = Styles.ERROR_COLOR;
        // close-button tooltip colors
        public static final Color HELP_CLOSE_TOOLTIP_BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color HELP_CLOSE_TOOLTIP_TEXT_COLOR = //
                HELP_CLOSE_TOOLTIP_BACKGROUND_COLOR.getReadableTextColor(4.5f);
        public static final Color HELP_CLOSE_TOOLTIP_OUTLINE_COLOR = Styles.OUTLINE;
        // shape
        public static final RoundedCorners ROUNDED = RoundedCorners.none();
        // layout
        public static final int ICON_PADDING = 3;
        public static final float TEXT_SCALE = 0.8f;
    }

    public static final class Input
    {
        // box colors
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color OUTLINE_COLOR = Styles.OUTLINE;
        // text colors
        public static final Color CARET_COLOR = Styles.TEXT;
        public static final Color TEXT_COLOR = Styles.TEXT;
        public static final Color DISABLED_TEXT = Styles.DISABLED_TEXT;
        public static final Color PLACEHOLDER_TEXT = Styles.DISABLED_TEXT;
        // selection colors
        public static final Color SELECTION_BACKGROUND = Color.fromInt(0xFF_93C5FD);
        public static final Color SELECTION_TEXT = Color.fromInt(0xFF_1E1E1E);
        // status colors
        public static final Color INFO_COLOR = Styles.INFO_COLOR;
        public static final Color SUCCESS_COLOR = Styles.SUCCESS_COLOR;
        public static final Color WARNING_COLOR = Styles.WARNING_COLOR;
        public static final Color ERROR_COLOR = Styles.ERROR_COLOR;
        public static final Color LOADER_COLOR = Color.fromInt(0xFF_3B82F6);
        // shape
        public static final RoundedCorners ROUNDED = RoundedCorners.none();
        // props
        public static final float OUTLINE_THICKNESS = Styles.THIN;
        public static final int TEXT_PADDING_X = 6;
        public static final int TEXT_PADDING_Y = 1;
        public static final int INDICATOR_PADDING_RIGHT = 4;
        public static final int CARET_BLINK_TIME = 500;
        public static final int ERROR_TOOLTIP_HEIGHT = 11;
        public static final int ERROR_TOOLTIP_SPACING = 2;
    }

    public static final class ResultData
    {
        // colors
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND.withAlpha(64);
        public static final Color OUTLINE_COLOR = Styles.BACKGROUND.withAlpha(164);
        public static final Color TEXT_COLOR = Styles.TEXT;

        public static final Color SELECTED_OUTLINE_COLOR = Styles.OUTLINE.withAlpha(108);
        public static final Color SELECTED_BACKGROUND_COLOR = SELECTED_OUTLINE_COLOR;

        public static final Color CLICKED_OUTLINE_COLOR = SELECTED_OUTLINE_COLOR;
        public static final Color CLICKED_BACKGROUND_COLOR = Styles.OUTLINE.withAlpha(92);

        public static final Color HOVER_OUTLINE_COLOR = Styles.OUTLINE.withAlpha(92);
        public static final Color HOVER_BACKGROUND_COLOR = Styles.OUTLINE.withAlpha(64);

        // props
        public static final boolean ROUNDED = false;
        public static final int PADDING_X = 7;
        public static final int PADDING_Y = 6;
        public static final float OUTLINE_THICKNESS = Styles.THIN;
    }

    public static final class ScrollBox
    {
        // outline
        public static final float OUTLINE_THICKNESS = Styles.THIN;
        // box colors
        public static final Color BACKGROUND_COLOR = Styles.BACKGROUND;
        public static final Color OUTLINE_COLOR = Styles.OUTLINE;
        // scrollbar colors
        public static final Color SCROLLBAR_COLOR = Styles.BACKGROUND;
        public static final Color SCROLLER_COLOR = Styles.OUTLINE;
        // shape
        public static final RoundedCorners ROUNDED = RoundedCorners.none();
        // props
        public static final int MARGIN = 3;
        public static final int SPACING = 3;
        public static final int SCROLLBAR_WIDTH = 6;
    }
}
