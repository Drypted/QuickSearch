package com.drypted.quicksearch.client.ui.handlers;

import com.drypted.quicksearch.client.core.actions.GiveItemAction;
import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import com.drypted.quicksearch.client.core.blueprints.commands.Command;
import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClickHandlers
{
    private final InputWidget inputWidget;

    public ClickHandlers(InputWidget inputWidget) { this.inputWidget = inputWidget; }

    public void onItemClicked(ItemsResultData data)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) GiveItemAction.run(player, data);
    }

    public void onCommandClicked(Command command)
    {
        if (command.validateArgs(CommandInputParser.getArgs(inputWidget.getText())).haltsExecution()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) command.execute(new String[]{}, player);
    }

    public void onCommandSuggestionClick(String suggestion)
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
