package com.drypted.spotlight.client.core.command;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TestArgsCommand implements Command
{
    @Override
    public boolean isNotArgs()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "args";
    }

    @Override
    public String getDescription()
    {
        return "Test With Args command.";
    }

    @Override
    public void execute(String[] args, LocalPlayer player)
    {
        player.displayClientMessage(
                Component.literal("Test With Args command executed with args \"" //
                        + String.join(", ", args) + "\""), true
        );
    }
}
