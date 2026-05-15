package com.drypted.quicksearch.client.core.result;

import com.drypted.quicksearch.client.core.blueprints.commands.Command;
import com.drypted.quicksearch.client.core.input.CommandInputParser;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class CommandResultClickHandler
{
    private final InputWidget inputWidget;

    public CommandResultClickHandler(InputWidget inputWidget)
    {
        this.inputWidget = inputWidget;
    }

    public void onCommandClicked(Command command)
    {
        if (command.validateArgs(CommandInputParser.getArgs(inputWidget.getText())).haltsExecution()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) command.execute(new String[]{}, player);
    }
}
