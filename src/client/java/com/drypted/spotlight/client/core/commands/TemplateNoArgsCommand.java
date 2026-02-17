package com.drypted.spotlight.client.core.commands;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TemplateNoArgsCommand implements Command
{
    @Override
    public boolean requiresArgs()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "no_args";
    }

    @Override
    public String getDescription()
    {
        return "Test No Args commands.";
    }

    @Override
    public CommandError validateArgs(String[] args)
    {
        return CommandError.NONE;
    }

    @Override
    public CommandError execute(String[] args, LocalPlayer player)
    {
        player.displayClientMessage(Component.literal("Test No Args commands executed."), true);

        return CommandError.NONE;
    }
}
