package com.drypted.spotlight.client.core.command;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TestCommand implements Command
{
    @Override
    public String getName()
    {
        return "test";
    }

    @Override
    public String getDescription()
    {
        return "A sample text command.";
    }

    @Override
    public void execute(String[] args, LocalPlayer player)
    {
        player.displayClientMessage(
                Component.literal("Test command executed with args \"" //
                        + String.join(", ", args) + "\""), true
        );
    }
}
