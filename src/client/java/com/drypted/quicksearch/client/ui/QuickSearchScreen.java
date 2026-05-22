package com.drypted.quicksearch.client.ui;

import com.drypted.quicksearch.client.QuickSearchClient;
import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.init.ModKeybinds;
import com.drypted.quicksearch.client.ui.components.HotbarCollectionWidget;
import com.drypted.quicksearch.client.ui.components.HotbarSlotWidget;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import com.drypted.quicksearch.client.ui.components.ScrollBoxWidget;
import com.drypted.quicksearch.client.ui.handlers.SubmitHandler;
import com.drypted.quicksearch.client.ui.handlers.mode.CommandsModeHandler;
import com.drypted.quicksearch.client.ui.handlers.mode.ItemModeHandler;
import com.drypted.quicksearch.client.ui.handlers.mode.ModeSelector;
import com.drypted.quicksearch.client.ui.state.ResultPresenter;
import com.drypted.quicksearch.client.ui.state.VisibilityController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class QuickSearchScreen extends Screen
{
    private static final int SEARCH_BAR_HEIGHT = 22;
    private static final int DISTANCE_FROM_CENTER = 20;
    private static final int HOTBAR_SLOTS = 9;

    // widgets
    private InputWidget inputWidget;
    private ScrollBoxWidget searchResultsWidget;
    private @Nullable HotbarCollectionWidget hotbarCollectionWidget;

    private VisibilityController visibilityController;
    private ResultPresenter resultPresenter;
    private ModeSelector modeSelector;
    private SubmitHandler submitHandler;

    private final boolean showCommandOnStartup;

    /// Keep track of the last query
    private static String lastQuery = "";

    public QuickSearchScreen(boolean showCommandOnStartup)
    {
        super(Component.literal("Spotlight Menu"));
        this.showCommandOnStartup = showCommandOnStartup;
    }

    @Override
    public void onClose()
    {
        lastQuery = this.inputWidget.getText();
        super.onClose();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void init()
    {
        final int searchBarWidth = QuickSearchClient.getConfig().ui.searchBarWidth;
        final int resultsHeight = QuickSearchClient.getConfig().ui.resultsBoxHeight;

        final int searchBarX = (this.width - searchBarWidth) / 2;
        final int searchBarY = (this.height - SEARCH_BAR_HEIGHT) / 2 - DISTANCE_FROM_CENTER;

        this.inputWidget = new InputWidget(searchBarX, searchBarY, searchBarWidth, SEARCH_BAR_HEIGHT);
        this.inputWidget.setPlaceholder("Search items or blocks ...");
        // set validator for no symbols
        this.inputWidget.setValidator(text -> text.matches("[/a-zA-Z0-9 -_\"]*"));

        this.searchResultsWidget = new ScrollBoxWidget(
                searchBarX,
                (int) (inputWidget.getY() + SEARCH_BAR_HEIGHT - inputWidget.getOutlineThickness()),
                inputWidget.getWidth(),
                resultsHeight
        );
        this.searchResultsWidget.setShowScrollerAlways(true);

        if (isHotbarEnabledInConfig())
        {
            this.hotbarCollectionWidget = HotbarCollectionWidget.create(
                    searchBarX,
                    searchBarWidth,
                    searchBarY - HotbarCollectionWidget.HOTBAR_SLOT_PADDING
            );
            this.addRenderableWidget(hotbarCollectionWidget);
        }

        // state and controllers
        this.visibilityController = new VisibilityController(
                inputWidget,
                searchResultsWidget,
                hotbarCollectionWidget,
                this::isHotbarEnabledInConfig
        );

        this.resultPresenter = new ResultPresenter(inputWidget, searchResultsWidget, visibilityController);

        CommandsModeHandler commandsModeHandler = new CommandsModeHandler(inputWidget, resultPresenter);
        ItemModeHandler itemModeHandler = new ItemModeHandler(resultPresenter);

        this.modeSelector = new ModeSelector(inputWidget, resultPresenter, commandsModeHandler, itemModeHandler);

        this.submitHandler = new SubmitHandler(inputWidget, resultPresenter, this::onClose);

        this.inputWidget.addTextChangeListener(this::onTextChanged);
        this.inputWidget.addSubmitListener((text) -> submitHandler.submit(text, isUserInputCommand()));

        this.addRenderableWidget(searchResultsWidget);
        this.addRenderableWidget(inputWidget);

        // show search on open
        visibilityController.setItemResultsVisible(false);

        this.setFocused(inputWidget);

        boolean isLastQueryCommand = lastQuery.startsWith("/");

        if (showCommandOnStartup)
        {
            if (isLastQueryCommand) inputWidget.setSuggestion(lastQuery);
            else inputWidget.setText("/"); // reset
        }
        else
        {
            if (isLastQueryCommand) inputWidget.setText(""); // reset
            else inputWidget.setSuggestion(lastQuery);
        }

        // ignore last query if config is set to not remember it
        if (!QuickSearchClient.getConfig().search.rememberLastQuery) lastQuery = "";

        // focus on input widget
        this.inputWidget.setFocused(true);
    }

    /* Input */

    private void onTextChanged(String text)
    {
        modeSelector.onTextChanged(text);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent kEv)
    {
        if (ModKeybinds.getCloseKey().matches(kEv))
        {
            // remove suggestion on close if config is set to not remember last query
            if (inputWidget.hasSuggestion() && !QuickSearchClient.getConfig().search.rememberLastQuery)
            {
                inputWidget.clearSuggestion();
                return true;
            }

            if (inputWidget.isFocused() && inputWidget.hasText())
            {
                inputWidget.clearText();
                resultPresenter.clearResults();
                visibilityController.setItemResultsVisible(false);
                return true;
            }
            this.onClose();
            return true;
        }

        // only allow key when hotbar is focused
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null && hotbarCollectionWidget.isFocused())
        {
            for (int i = 0; i < HOTBAR_SLOTS; i++)
            {
                HotbarSlotWidget hotbarWidget = this.hotbarCollectionWidget.getWidgets().get(i);
                if (hotbarWidget != null && kEv.key() == this.minecraft.options.keyHotbarSlots[i].getDefaultKey()
                                                                                                 .getValue())
                {
                    this.hotbarCollectionWidget.onHotbarKeyPressed(hotbarWidget, kEv.modifiers());
                    return true;
                }
            }
        }

        return super.keyPressed(kEv);
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent kEv)
    {
        if (isHotbarEnabledInConfig() && this.hotbarCollectionWidget != null)
            this.hotbarCollectionWidget.onAnyKeyReleased();

        return super.keyReleased(kEv);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mEv, boolean doubleClick)
    {
        if (!super.mouseClicked(mEv, doubleClick))
        {
            // click outside, close spotlight
            this.onClose();
            return true;
        }
        return super.mouseClicked(mEv, doubleClick);
    }

    /* Overrides for settings */

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int i, int j, float f)
    {
    } // don't render any background

    @Override
    public boolean isPauseScreen() { return false; }

    /* Helpers */

    private boolean isHotbarEnabledInConfig() { return QuickSearchClient.getConfig().hotbar.showHotbarSlots; }

    private boolean isUserInputCommand() { return CommandInputParser.isCommandInput(inputWidget.getText()); }
}