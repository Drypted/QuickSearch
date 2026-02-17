package com.drypted.spotlight.client.core.commands;

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
        return "Test With Args commands.";
    }

    @Override
    public CommandError validateArgs(String[] args)
    {
        if (args.length != 1) return CommandError.withError("Please input only one argument");

        if (!args[0].matches("^[a-zA-Z]+$"))
        {
            return CommandError.withError("Please input only alphabets");
        }

        return CommandError.NONE;
    }

    @Override
    public CommandError execute(String[] args, LocalPlayer player)
    {
        CommandError error = validateArgs(args);

        if (!error.isIgnorable())
        {
            return error;
        }

        player.displayClientMessage(
                Component.literal("Test With Args commands executed with args \"" //
                        + String.join(", ", args) + "\""), true
        );

        return CommandError.NONE;
    }
}
