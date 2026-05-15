package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.input.CommandInputParser;
import com.drypted.spotlight.client.ui.components.InputWidget;

public final class SpotlightSuggestionApplyHandler
{
    private final InputWidget inputWidget;

    public SpotlightSuggestionApplyHandler(InputWidget inputWidget)
    {
        this.inputWidget = inputWidget;
    }

    public void applySuggestion(String suggestion)
    {
        String currentText = inputWidget.getText();
        String[] args = CommandInputParser.getArgs(currentText);
        String currentPartial = args.length > 0 ? args[args.length - 1] : "";

        String newText;
        if (!currentPartial.isEmpty())
        {
            newText = currentText.substring(0, currentText.length() - currentPartial.length()) + suggestion;
        }
        else
        {
            newText = currentText + suggestion;
        }

        inputWidget.setText(newText);
    }
}
