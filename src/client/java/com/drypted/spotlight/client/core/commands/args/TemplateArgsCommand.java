package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TemplateArgsCommand implements Command
{
    @Override
    public boolean requiresArgs()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "args";
    }

    @Override
    public String getDescription()
    {
        return "Test With Args commands.";
    }

    @Override
    public CommandFeedback validateArgs(String[] args)
    {
        if (args.length != 1) return CommandFeedback.withError("Please input only one argument");

        if (!args[0].matches("^[a-zA-Z]+$"))
        {
            return CommandFeedback.withError("Please input only alphabets");
        }

        return CommandFeedback.NO_ERROR;
    }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback error = validateArgs(args);

        if (error.isCritical())
        {
            return error;
        }

        player.displayClientMessage(
                Component.literal("Test With Args commands executed with args \"" //
                        + String.join(", ", args) + "\""), true
        );

        return CommandFeedback.NO_ERROR;
    }
}
