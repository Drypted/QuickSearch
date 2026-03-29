package com.drypted.spotlight.client.ui.components;

import com.drypted.spotlight.client.SpotlightClient;
import com.drypted.spotlight.client.config.ModConfig;
import com.drypted.spotlight.client.core.blueprints.feedback.InputError;
import com.drypted.spotlight.client.core.blueprints.ui.common.Color;
import com.drypted.spotlight.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.spotlight.client.init.ModKeybinds;
import com.drypted.spotlight.client.ui.renderer.RenderCommon;
import com.drypted.spotlight.client.ui.styling.Styles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InputWidget extends AbstractWidget
{
    private static final Font FONT = Minecraft.getInstance().font;
    private static final int TEXT_PADDING_X = 6;
    private static final int TEXT_PADDING_Y = 1;
    private static final int INDICATOR_PADDING_RIGHT = 4;
    private static final int CARET_BLINK_TIME = 500;
    private static final int ERROR_TOOLTIP_HEIGHT = 11;
    private static final int ERROR_TOOLTIP_SPACING = 2;

    // Visual configuration
    private final float outlineThickness;
    private final Color backgroundColor;
    private final Color outlineColor;
    private final Color caretColor;
    private final Color normalTextColor;
    private final Color disabledTextColor;
    private final Color selectionBackgroundColor;
    private final Color selectionTextColor;
    private final Color placeholderColor;

    private RoundedCorners rounded;

    // Text state
    private String text = "";
    private int maxLength = 256;
    private String placeholder = "";
    private String suggestion = "";

    // Caret and selection
    private int cursorPos = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long caretTime = 0;

    // Scrolling
    private int scrollOffset = 0;

    // Widget states
    private SearchStatus searchStatus = SearchStatus.IDLE;
    private boolean isDisabled = false;
    private boolean isReadOnly = false;
    private boolean hasError = false;
    private @Nullable InputError error;

    // Mouse selection
    private boolean isDragging = false;
    private int dragStartPos = -1;

    // Callbacks
    private final ArrayList<Consumer<String>> onTextChangeCallbacks = new ArrayList<>();
    private final ArrayList<Consumer<String>> onSubmitCallbacks = new ArrayList<>();
    private final ArrayList<Consumer<Boolean>> onFocusChangeCallbacks = new ArrayList<>();
    private Predicate<String> validator = null;

    private final Color LoaderColor = Styles.Input.LOADER_COLOR;

    public InputWidget(int x, int y, int width, int height, RoundedCorners rounded, float outlineThickness, Color backgroundColor, Color outlineColor, Color caretColor, Color normalTextColor, Color disabledTextColor, Color selectionBackgroundColor, Color selectionTextColor, Color placeholderColor)
    {
        super(x, y, width, height, Component.empty());
        this.rounded = rounded;
        this.outlineThickness = outlineThickness;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
        this.caretColor = caretColor;
        this.normalTextColor = normalTextColor;
        this.disabledTextColor = disabledTextColor;
        this.selectionBackgroundColor = selectionBackgroundColor;
        this.selectionTextColor = selectionTextColor;
        this.placeholderColor = placeholderColor;
    }

    @Override
    protected void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // Determine outline color based on state
        Color currentOutlineColor = shouldShowError()
                                    ? Objects.requireNonNull(this.error).getColor()
                                    : this.outlineColor;

        // Background
        RenderCommon.drawRectangle(
                guiGraphics,
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                this.rounded,
                this.outlineThickness,
                true,
                this.backgroundColor,
                currentOutlineColor
        );

        // Calculate text rendering area
        int textX = getTextX();
        int textY = getTextY();
        int textAreaWidth = getTextAreaWidth();
        int textAreaHeight = this.getHeight();

        // Enable scissor for clipping
        guiGraphics.enableScissor(textX, this.getY(), textX + textAreaWidth, this.getY() + textAreaHeight);

        // Render text or placeholder
        if (this.text.isEmpty() && this.suggestion.isEmpty() && !this.placeholder.isEmpty())
        {
            // Render placeholder
            guiGraphics.drawString(FONT, this.placeholder, textX, textY, placeholderColor.asInt(), false);
        }
        else
        {
            // Render selection highlight
            if (hasSelection())
            {
                drawSelection(guiGraphics, textX, textY);
            }

            // separate text into three parts: before selection, selection, after selection
            String beforeSelection;
            String selection;
            String afterSelection;
            try
            {
                final int start = Math.min(selectionStart, selectionEnd);
                final int end = Math.max(selectionStart, selectionEnd);

                beforeSelection = this.text.substring(0, start);
                selection = this.text.substring(start, end);
                afterSelection = this.text.substring(end);
            }
            catch (Exception e)
            {
                beforeSelection = this.text;
                selection = "";
                afterSelection = "";
            }

            if (shouldShowSuggestion())
            {
                String ghostText;

                if (suggestionIsCompletion())
                {
                    // completion
                    ghostText = suggestion.substring(text.toLowerCase().length());
                }
                else
                {
                    // independent ghost text
                    ghostText = suggestion;
                }

                int ghostX = textX + FONT.width(this.text) - scrollOffset;
                guiGraphics.drawString(FONT, ghostText, ghostX, textY, placeholderColor.asInt(), false);
            }

            // Render text
            // Priority: disabled > message > normal
            Color textColor = isDisabled
                              ? disabledTextColor
                              : shouldShowError() ? Objects.requireNonNull(this.error).getColor() : normalTextColor;
            guiGraphics.drawString(FONT, beforeSelection, textX - scrollOffset, textY, textColor.asInt(), false);

            guiGraphics.drawString(
                    FONT,
                    selection,
                    (textX + FONT.width(beforeSelection)) - scrollOffset,
                    textY,
                    selectionTextColor.asInt(),
                    false
            );

            guiGraphics.drawString(
                    FONT,
                    afterSelection,
                    (textX + FONT.width(beforeSelection) + FONT.width(selection)) - scrollOffset,
                    textY,
                    textColor.asInt(),
                    false
            );
        }

        // Render caret
        if (this.isFocused() && !isReadOnly && shouldDrawCaret())
        {
            drawCaret(guiGraphics, textX, textY);
        }

        // Disable scissor
        guiGraphics.disableScissor();

        // Render status indicators
        switch (this.searchStatus)
        {
            case SEARCHING:
                drawLoadingAtEnd(guiGraphics);
                break;
            case IDLE:
            default:
                break;
        }

        // Render message
        if (shouldShowError())
        {
            assert this.error != null;
            RenderCommon.drawLabelWithScale(
                    guiGraphics,
                    error.getMessage(),
                    0.65f,
                    this.getX(),
                    this.getY() - ERROR_TOOLTIP_HEIGHT - ERROR_TOOLTIP_SPACING,
                    this.getX() + this.getWidth(),
                    this.getY() - ERROR_TOOLTIP_SPACING,
                    RoundedCorners.all(),
                    1,
                    this.error.getColor(),
                    normalTextColor
            );
        }
    }

    /* Drawing Methods */

    private void drawCaret(GuiGraphics guiGraphics, int textX, int textY)
    {
        String textBeforeCursor = this.text.substring(0, cursorPos);
        int caretX = textX + FONT.width(textBeforeCursor) - scrollOffset;

        // Only draw if caret is within visible area
        int textAreaWidth = getTextAreaWidth();
        if (caretX >= textX && caretX < textX + textAreaWidth)
        {
            guiGraphics.fill(caretX, textY, caretX + 1, textY + FONT.lineHeight, caretColor.asInt());
        }
    }

    private void drawSelection(GuiGraphics guiGraphics, int textX, int textY)
    {
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);

        String textBeforeStart = this.text.substring(0, start);
        String selectedText = this.text.substring(start, end);

        int selectionX = textX + FONT.width(textBeforeStart) - scrollOffset;
        int selectionWidth = FONT.width(selectedText);

        // Clip selection to visible area
        int textAreaWidth = getTextAreaWidth();
        int visibleSelectionX = Math.max(selectionX, textX);
        int visibleSelectionEnd = Math.min(selectionX + selectionWidth, textX + textAreaWidth);
        int visibleSelectionWidth = visibleSelectionEnd - visibleSelectionX;

        if (visibleSelectionWidth > 0)
        {
            guiGraphics.fill(
                    visibleSelectionX,
                    textY,
                    visibleSelectionX + visibleSelectionWidth,
                    textY + FONT.lineHeight,
                    selectionBackgroundColor.asInt()
            );
        }
    }

    private void drawLoadingAtEnd(GuiGraphics guiGraphics)
    {
        int size = this.height - (2 * INDICATOR_PADDING_RIGHT);
        int loadingX = this.getX() + this.getWidth() - INDICATOR_PADDING_RIGHT - size;
        int loadingY = this.getY() + INDICATOR_PADDING_RIGHT;

        RenderCommon.drawThreeDotPulseSpinner(
                guiGraphics,
                loadingX,
                loadingY,
                size,
                LoaderColor,
                System.currentTimeMillis()
        );
    }

    /* Helper Methods */

    private int getTextX()
    {
        return this.getX() + TEXT_PADDING_X;
    }

    private int getTextY()
    {
        return this.getY() + (this.height - FONT.lineHeight) / 2 + TEXT_PADDING_Y;
    }

    private int getTextAreaWidth()
    {
        int indicatorSpace = (searchStatus == SearchStatus.SEARCHING)
                             ? (this.height - INDICATOR_PADDING_RIGHT + INDICATOR_PADDING_RIGHT)
                             : 0;
        return this.getWidth() - (TEXT_PADDING_X * 2) - indicatorSpace;
    }

    private boolean shouldDrawCaret()
    {
        long elapsed = System.currentTimeMillis() - caretTime;
        return (elapsed / CARET_BLINK_TIME) % 2 == 0;
    }

    /* Text Editing */


    @Override
    public boolean charTyped(@NonNull CharacterEvent chEv)
    {
        if (!this.isFocused() || isDisabled || isReadOnly) return false;

        if (StringUtil.isAllowedChatCharacter(chEv.codepoint()))
        {
            insertText(chEv.codepointAsString());
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent)
    {
        if (!this.isFocused() || isDisabled) return false;

        boolean ctrl = (keyEvent.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 || (keyEvent.modifiers() & GLFW.GLFW_MOD_SUPER) != 0;
        boolean shift = (keyEvent.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0;

        // Accept suggestion with Tab (only if cursor is at end and no selection)
        if (keyEvent.key() == GLFW.GLFW_KEY_TAB && shouldShowSuggestion())
        {
            switch (SpotlightClient.getConfig().search.completionType)
            {
                case NONE -> { }
                case SINGLE_WORD ->
                {
                    String remaining;

                    if (suggestionIsCompletion()) remaining = suggestion.substring(text.length());
                    else remaining = suggestion;

                    if (!remaining.isEmpty())
                    {
                        // insert the next word + space (if any word after this one)
                        insertText(remaining.contains(" ")
                                   ? remaining.substring(0, remaining.indexOf(" ") + 1)
                                   : remaining);

                    }
                }
                case WHOLE_QUERY ->
                    // Complete the entire suggestion
                        setText(suggestion);
                case null, default ->
                {
                    // pass
                }
            }
            clearSuggestion(); // Clear suggestion after accepting
            return true;
        }

        // Clipboard operations
        if (ctrl)
        {
            switch (keyEvent.key())
            {
                case GLFW.GLFW_KEY_C:
                    copyToClipboard();
                    return true;
                case GLFW.GLFW_KEY_X:
                    if (!isReadOnly)
                    {
                        cutToClipboard();
                    }
                    return true;
                case GLFW.GLFW_KEY_V:
                    if (!isReadOnly)
                    {
                        pasteFromClipboard();
                    }
                    return true;
                case GLFW.GLFW_KEY_A:
                    selectAll();
                    return true;
            }
        }

        if (isReadOnly && (keyEvent.key() == GLFW.GLFW_KEY_BACKSPACE || keyEvent.key() == GLFW.GLFW_KEY_DELETE))
            return false;

        if (ModKeybinds.getInputSubmitKey().matches(keyEvent))
        {
            submit();
            return true;
        }

        // Movement and editing
        return switch (keyEvent.key())
        {
            case GLFW.GLFW_KEY_LEFT ->
            {
                moveCursorLeft(ctrl, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT ->
            {
                moveCursorRight(ctrl, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME ->
            {
                moveCursorToStart(shift);
                yield true;
            }
            case GLFW.GLFW_KEY_END ->
            {
                moveCursorToEnd(shift);
                yield true;
            }
            case GLFW.GLFW_KEY_BACKSPACE ->
            {
                handleBackspace(ctrl);
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE ->
            {
                handleDelete(ctrl);
                yield true;
            }
            default -> false;
        };

    }

    private boolean suggestionIsCompletion()
    {
        return suggestion.toLowerCase().startsWith(text.toLowerCase()) && !suggestion.equalsIgnoreCase(text);
    }

    /* Cursor Movement */

    private void moveCursorLeft(boolean byWord, boolean selecting)
    {
        if (byWord)
        {
            moveCursorByWord(-1, selecting);
        }
        else
        {
            int newPos = Math.max(0, cursorPos - 1);
            setCursorPosition(newPos, selecting);
        }
    }

    private void moveCursorRight(boolean byWord, boolean selecting)
    {
        if (byWord)
        {
            moveCursorByWord(1, selecting);
        }
        else
        {
            int newPos = Math.min(text.length(), cursorPos + 1);
            setCursorPosition(newPos, selecting);
        }
    }

    private void moveCursorByWord(int direction)
    {
        moveCursorByWord(direction, false);
    }

    private void moveCursorByWord(int direction, boolean selecting)
    {
        int newPos;

        if (direction < 0)
        {
            // Move left by word
            newPos = findPreviousWordBoundary(cursorPos);
        }
        else
        {
            // Move right by word
            newPos = findNextWordBoundary(cursorPos);
        }

        setCursorPosition(newPos, selecting);
    }

    private void moveCursorToStart(boolean selecting)
    {
        setCursorPosition(0, selecting);
    }

    private void moveCursorToEnd(boolean selecting)
    {
        setCursorPosition(text.length(), selecting);
    }

    private void setCursorPosition(int pos, boolean selecting)
    {
        pos = Math.max(0, Math.min(text.length(), pos));

        if (selecting)
        {
            if (selectionStart == -1)
            {
                selectionStart = cursorPos;
            }
            selectionEnd = pos;
        }
        else
        {
            clearSelection();
        }

        cursorPos = pos;
        updateScrollOffset();
        resetCaretBlink();
    }

    /* Word Boundaries */

    private int findPreviousWordBoundary(int from)
    {
        if (from <= 0) return 0;

        int pos = from - 1;

        // Skip whitespace
        while (pos > 0 && Character.isWhitespace(text.charAt(pos)))
        {
            pos--;
        }

        // Skip word characters
        while (pos > 0 && !Character.isWhitespace(text.charAt(pos - 1)))
        {
            pos--;
        }

        return pos;
    }

    private int findNextWordBoundary(int from)
    {
        if (from >= text.length()) return text.length();

        int pos = from;

        // Skip current word
        while (pos < text.length() && !Character.isWhitespace(text.charAt(pos)))
        {
            pos++;
        }

        // Skip whitespace
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
        {
            pos++;
        }

        return pos;
    }

    /* Selection */

    private boolean hasSelection()
    {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private void clearSelection()
    {
        selectionStart = -1;
        selectionEnd = -1;
    }

    private void selectAll()
    {
        selectionStart = 0;
        selectionEnd = text.length();
        cursorPos = text.length();
        updateScrollOffset();
    }

    private String getSelectedText()
    {
        if (!hasSelection()) return "";

        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        return text.substring(start, end);
    }

    private void deleteSelection()
    {
        if (!hasSelection()) return;

        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);

        text = text.substring(0, start) + text.substring(end);
        cursorPos = start;
        clearSelection();
        notifyTextChanged();
    }

    /* Text Insertion and Deletion */

    private void insertText(String str)
    {
        if (hasSelection())
        {
            deleteSelection();
        }

        // Check max length
        if (text.length() + str.length() > maxLength)
        {
            str = str.substring(0, maxLength - text.length());
            if (str.isEmpty()) return;
        }

        text = text.substring(0, cursorPos) + str + text.substring(cursorPos);
        cursorPos += str.length();
        clearSelection();
        updateScrollOffset();
        resetCaretBlink();
        notifyTextChanged();
    }

    private void handleBackspace(boolean byWord)
    {
        if (hasSelection())
        {
            deleteSelection();
            updateScrollOffset();
            resetCaretBlink();
            return;
        }

        if (cursorPos == 0) return;

        if (byWord)
        {
            int newPos = findPreviousWordBoundary(cursorPos);
            text = text.substring(0, newPos) + text.substring(cursorPos);
            cursorPos = newPos;
        }
        else
        {
            text = text.substring(0, cursorPos - 1) + text.substring(cursorPos);
            cursorPos--;
        }

        updateScrollOffset();
        resetCaretBlink();
        notifyTextChanged();
    }

    private void handleDelete(boolean byWord)
    {
        if (hasSelection())
        {
            deleteSelection();
            updateScrollOffset();
            resetCaretBlink();
            return;
        }

        if (cursorPos >= text.length()) return;

        if (byWord)
        {
            int newPos = findNextWordBoundary(cursorPos);
            text = text.substring(0, cursorPos) + text.substring(newPos);
        }
        else
        {
            text = text.substring(0, cursorPos) + text.substring(cursorPos + 1);
        }

        updateScrollOffset();
        resetCaretBlink();
        notifyTextChanged();
    }

    /* Clipboard */

    private void copyToClipboard()
    {
        if (!hasSelection()) return;

        String selected = getSelectedText();
        Minecraft.getInstance().keyboardHandler.setClipboard(selected);
    }

    private void cutToClipboard()
    {
        if (!hasSelection()) return;

        copyToClipboard();
        deleteSelection();
        updateScrollOffset();
        resetCaretBlink();
    }

    private void pasteFromClipboard()
    {
        String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (clipboard.isEmpty()) return;

        // Filter allowed characters
        StringBuilder filtered = new StringBuilder();
        for (char c : clipboard.toCharArray())
        {
            if (StringUtil.isAllowedChatCharacter(c))
            {
                filtered.append(c);
            }
        }

        if (!filtered.isEmpty())
        {
            insertText(filtered.toString());
        }
    }

    /* Scrolling */

    private void updateScrollOffset()
    {
        String textBeforeCursor = text.substring(0, cursorPos);
        int caretPixelPos = FONT.width(textBeforeCursor);
        int textAreaWidth = getTextAreaWidth();

        // Scroll right if caret is beyond visible area
        if (caretPixelPos - scrollOffset > textAreaWidth - 5)
        {
            scrollOffset = caretPixelPos - textAreaWidth + 5;
        }

        // Scroll left if caret is before visible area
        if (caretPixelPos - scrollOffset < 5)
        {
            scrollOffset = Math.max(0, caretPixelPos - 5);
        }

        // Clamp scroll offset
        int maxScroll = Math.max(0, FONT.width(text) - textAreaWidth);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private void resetCaretBlink()
    {
        caretTime = System.currentTimeMillis();
    }

    /* Mouse Interaction */

    @Override
    public boolean mouseClicked(MouseButtonEvent mEv, boolean doubleClick)
    {
        if (!this.isMouseOver(mEv.x(), mEv.y()))
        {
            if (this.isFocused())
            {
                setFocused(false);
            }

            return false;
        }

        if (isDisabled) return false;

        // Left click
        if (mEv.button() == 0)
        {
            setFocused(true);

            // Single click - place caret
            int clickPos = getCharacterIndexAt(mEv.x());
            setCursorPosition(clickPos, false);
            updateScrollOffset();
            isDragging = true;
            dragStartPos = clickPos;

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent mEv, double dragX, double dragY)
    {
        if (!isDragging || isDisabled) return false;

        double clampedX = Math.max(getTextX(), mEv.x());
        int currentPos = getCharacterIndexAt(clampedX);
        selectionStart = dragStartPos;
        selectionEnd = currentPos;
        cursorPos = currentPos;
        updateScrollOffset();

        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent mEv)
    {
        if (mEv.button() == 0 && isDragging)
        {
            isDragging = false;
            return true;
        }
        return false;
    }

    private int getCharacterIndexAt(double mouseX)
    {
        int textX = getTextX();
        int relativeX = (int) mouseX - textX + scrollOffset;

        if (relativeX <= 0) return 0;

        int width = 0;
        for (int i = 0; i < text.length(); i++)
        {
            int charWidth = FONT.width(String.valueOf(text.charAt(i)));
            if (relativeX < width + charWidth)
            {
                return i;
            }
            width += charWidth;
        }

        return text.length();
    }

    private void selectWordAt(int pos)
    {
        if (text.isEmpty()) return;

        pos = Math.max(0, Math.min(text.length() - 1, pos));

        int start = pos;
        int end = pos;

        // Expand to word boundaries
        while (start > 0 && !Character.isWhitespace(text.charAt(start - 1)))
        {
            start--;
        }

        while (end < text.length() && !Character.isWhitespace(text.charAt(end)))
        {
            end++;
        }

        selectionStart = start;
        selectionEnd = end;
        cursorPos = end;
        updateScrollOffset();
    }

    /* Focus */

    @Override
    public void setFocused(boolean focused)
    {
        boolean wasFocused = this.isFocused();
        super.setFocused(focused);

        if (focused && !wasFocused)
        {
            resetCaretBlink();
        }

        if (wasFocused != focused)
        {
            if (!focused)
            {
                clearSelection();
            }

            for (Consumer<Boolean> callback : onFocusChangeCallbacks)
            {
                callback.accept(focused);
            }
        }
    }

    /* Submission */

    private void submit()
    {
        for (Consumer<String> callback : onSubmitCallbacks)
        {
            callback.accept(text);
        }
    }

    /* Validation */

    private void notifyTextChanged()
    {
        // Clear suggestion when text changes
        clearSuggestion();

        // Validate
        if (validator != null)
        {
            hasError = !validator.test(text);
        }

        // Notify callbacks
        for (Consumer<String> callback : onTextChangeCallbacks)
        {
            callback.accept(text);
        }
    }

    /* Accessibility */

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output)
    {
        if (isDisabled)
        {
            output.add(NarratedElementType.TITLE, Component.literal("Search input (disabled)"));
        }
        else if (isReadOnly)
        {
            output.add(NarratedElementType.TITLE, Component.literal("Search input (read-only): " + text));
        }
        else
        {
            String narration = "Search input";

            if (!text.isEmpty())
            {
                narration += ": " + text;
            }
            else if (!placeholder.isEmpty())
            {
                narration += " (placeholder: " + placeholder + ")";
            }

            if (hasSelection())
            {
                narration += ". Selected: " + getSelectedText();
            }
            else
            {
                narration += ". Cursor at position " + cursorPos;
            }

            if (hasError)
            {
                narration += ". Invalid input";
            }

            if (Objects.requireNonNull(searchStatus) == SearchStatus.SEARCHING)
            {
                narration += ". Searching";
            }

            output.add(NarratedElementType.TITLE, Component.literal(narration));
        }
    }

    /* Getters and Setters */

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text == null ? "" : text.substring(0, Math.min(text.length(), maxLength));
        this.cursorPos = Math.max(cursorPos, this.text.length());
        clearSelection();
        updateScrollOffset();
        notifyTextChanged();
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public boolean hasSuggestion()
    {
        return this.shouldShowSuggestion();
    }

    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    public void clearSuggestion()
    {
        suggestion = "";
    }

    public SearchStatus getSearchStatus()
    {
        return searchStatus;
    }

    public void setSearchStatus(SearchStatus searchStatus)
    {
        this.searchStatus = searchStatus;
    }

    public boolean isDisabled()
    {
        return isDisabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.isDisabled = disabled;
        if (disabled && this.isFocused())
        {
            setFocused(false);
        }
    }

    public boolean isReadOnly()
    {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.isReadOnly = readOnly;
    }

    public String getPlaceholder()
    {
        return placeholder;
    }

    public void setPlaceholder(String placeholder)
    {
        this.placeholder = placeholder == null ? "" : placeholder;
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = Math.max(1, maxLength);
        if (text.length() > this.maxLength)
        {
            setText(text.substring(0, this.maxLength));
        }
    }

    public void setValidator(Predicate<String> validator)
    {
        this.validator = validator;
        notifyTextChanged(); // Re-validate current text
    }

    public float getOutlineThickness()
    {
        return outlineThickness;
    }

    public RoundedCorners getRounded()
    {
        return rounded;
    }

    public void setRounded(RoundedCorners rounded)
    {
        this.rounded = rounded;
    }

    /* ERROR */

    public Predicate<String> getValidator()
    {
        return validator;
    }

    public boolean hasError()
    {
        return hasError;
    }

    public void showError(InputError error)
    {
        this.error = error;
        this.hasError = true;
    }

    public void clearError()
    {
        this.error = null;
        this.hasError = false;
    }

    public @Nullable InputError getError()
    {
        return error;
    }

    private boolean shouldShowError()
    {
        return this.hasError && this.error != null && this.error.getMessage() != null && !this.error.getMessage()
                .isEmpty();
    }

    /* Public Methods */

    public void clearText()
    {
        this.text = "";
        this.cursorPos = 0;
        clearSelection();
        scrollOffset = 0;
        notifyTextChanged();
    }

    public boolean hasText()
    {
        return !this.text.isEmpty();
    }

    /* Utils */

    private boolean shouldShowSuggestion()
    {
        return SpotlightClient.getConfig().search.completionType != ModConfig.CompletionType.NONE // not disabled
                && !suggestion.isEmpty() // not empty
                && cursorPos == text.length() // cursor at end
                && !hasSelection()  // not selecting
                ;
    }

    /* Callbacks */

    public void addTextChangeListener(Consumer<String> listener)
    {
        this.onTextChangeCallbacks.add(listener);
    }

    public void addSubmitListener(Consumer<String> listener)
    {
        this.onSubmitCallbacks.add(listener);
    }

    public void addFocusChangeListener(Consumer<Boolean> listener)
    {
        this.onFocusChangeCallbacks.add(listener);
    }

    /* Builder */

    public static Builder builder(int x, int y, int width, int height)
    {
        return new Builder(x, y, width, height);
    }

    public static final class Builder
    {
        private final int x;
        private final int y;
        private final int width;
        private int height;
        private RoundedCorners rounded = RoundedCorners.all();
        private float outlineThickness = Styles.Input.OUTLINE_THICKNESS;
        private Color backgroundColor = Styles.Input.BACKGROUND_COLOR;
        private Color outlineColor = Styles.Input.OUTLINE_COLOR;
        private Color caretColor = Styles.Input.CARET_COLOR;
        private Color normalTextColor = Styles.Input.TEXT_COLOR;
        private Color disabledTextColor = Styles.Input.DISABLED_TEXT;
        private Color selectionBackgroundColor = Styles.Input.SELECTION_BACKGROUND;
        private Color selectionTextColor = Styles.Input.SELECTION_TEXT;
        private Color placeholderColor = Styles.Input.PLACEHOLDER_TEXT;
        private String placeholder = "";
        private int maxLength = 256;
        private Predicate<String> validator = null;

        public Builder(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder height(int height)
        {
            this.height = height;
            return this;
        }

        public Builder rounded(RoundedCorners rounded)
        {
            this.rounded = rounded;
            return this;
        }

        public Builder outlineThickness(int outlineThickness)
        {
            this.outlineThickness = outlineThickness;
            return this;
        }

        public Builder backgroundColor(Color backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder outlineColor(Color outlineColor)
        {
            this.outlineColor = outlineColor;
            return this;
        }

        public Builder caretColor(Color caretColor)
        {
            this.caretColor = caretColor;
            return this;
        }

        public Builder normalTextColor(Color normalTextColor)
        {
            this.normalTextColor = normalTextColor;
            return this;
        }

        public Builder disabledTextColor(Color disabledTextColor)
        {
            this.disabledTextColor = disabledTextColor;
            return this;
        }

        public Builder selectionBackgroundColor(Color selectionBackgroundColor)
        {
            this.selectionBackgroundColor = selectionBackgroundColor;
            return this;
        }

        public Builder selectionTextColor(Color selectionTextColor)
        {
            this.selectionTextColor = selectionTextColor;
            return this;
        }

        public Builder placeholderColor(Color placeholderColor)
        {
            this.placeholderColor = placeholderColor;
            return this;
        }

        public Builder placeholder(String placeholder)
        {
            this.placeholder = placeholder;
            return this;
        }

        public Builder maxLength(int maxLength)
        {
            this.maxLength = maxLength;
            return this;
        }

        public Builder validator(Predicate<String> validator)
        {
            this.validator = validator;
            return this;
        }

        public InputWidget build()
        {
            InputWidget widget = new InputWidget(
                    x,
                    y,
                    width,
                    height,
                    rounded,
                    outlineThickness,
                    backgroundColor,
                    outlineColor,
                    caretColor,
                    normalTextColor,
                    disabledTextColor,
                    selectionBackgroundColor,
                    selectionTextColor,
                    placeholderColor
            );
            widget.setPlaceholder(placeholder);
            widget.setMaxLength(maxLength);
            widget.setValidator(validator);
            return widget;
        }
    }

    /* Inner Classes */

    public enum SearchStatus
    {
        IDLE,
        SEARCHING
    }
}
