package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
import com.drypted.spotlight.client.core.storage.HotbarStorage;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;

import java.io.IOException;

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
        return "Save the current hotbar combination";
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
        CommandFeedback argsError = validateArgs(args);
        if (!argsError.isNone()) return argsError;

        String name = args[0].toLowerCase();
        RegistryAccess registryAccess = player.level().registryAccess();

        try
        {
            HotbarStorage.save(name, player.getInventory(), registryAccess);
            return CommandFeedback.withInfo("Saved hotbar as \"" + name + "\"");
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to save hotbar: " + e.getMessage());
        }
    }
}
