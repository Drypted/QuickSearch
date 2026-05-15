package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.blueprints.commands.Command;
import com.drypted.spotlight.client.core.input.CommandInputParser;
import com.drypted.spotlight.client.ui.components.InputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class SpotlightCommandResultClickHandler
{
    private final InputWidget inputWidget;

    public SpotlightCommandResultClickHandler(InputWidget inputWidget)
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
