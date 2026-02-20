package com.drypted.spotlight.client.core.commands.no_args;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
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
    public CommandFeedback validateArgs(String[] args)
    {
        return CommandFeedback.NO_ERROR;
    }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        player.displayClientMessage(Component.literal("Test No Args commands executed."), true);

        return CommandFeedback.NO_ERROR;
    }
}
