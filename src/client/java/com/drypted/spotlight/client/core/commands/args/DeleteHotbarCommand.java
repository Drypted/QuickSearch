package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.spotlight.client.core.blueprints.commands.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.commands.argument.types.StringOptionArgumentType;
import com.drypted.spotlight.client.core.storage.HotbarStorage;
import net.minecraft.client.player.LocalPlayer;

import java.io.IOException;

public class DeleteHotbarCommand extends ArgumentedCommand
{
    public DeleteHotbarCommand() throws IOException
    {
        super(new StringOptionArgumentType(HotbarStorage.getStoredNames()));
    }

    @Override
    public String getName()
    {
        return "delh";
    }

    @Override
    public String getDescription()
    {
        return "Delete a saved hotbar combination";
    }


    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isNotNone()) return argsError;


        String name = args[0].toLowerCase();
        try
        {
            HotbarStorage.remove(name);
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to remove hotbar: " + e.getMessage());
        }

        return CommandFeedback.withSuccess("Removed hotbar \"" + name + "\"");
    }
}
