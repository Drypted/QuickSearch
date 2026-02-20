package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
import net.minecraft.client.player.LocalPlayer;

public class SaveHotbarCommand implements Command
{
    @Override
    public boolean requiresArgs()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "saveh";
    }

    @Override
    public String getDescription()
    {
        return "Save the current hotbar hotbar combination";
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
        return CommandFeedback.withInfo("Not Implemented: " + getDescription());
    }
}
