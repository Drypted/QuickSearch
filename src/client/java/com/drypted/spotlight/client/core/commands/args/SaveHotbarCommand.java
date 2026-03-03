package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.commands.argument.types.WordArgumentType;
import com.drypted.spotlight.client.core.storage.HotbarStorage;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;

import java.io.IOException;

public class SaveHotbarCommand extends ArgumentedCommand
{
    public SaveHotbarCommand()
    {
        super(new WordArgumentType("<name>"));
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
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isNotNone()) return argsError;

        String name = args[0].toLowerCase();
        RegistryAccess registryAccess = player.level().registryAccess();

        try
        {
            HotbarStorage.save(name, player.getInventory(), registryAccess);
            return CommandFeedback.withSuccess("Saved hotbar as \"" + name + "\"");
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to save hotbar: " + e.getMessage());
        }
    }
}
