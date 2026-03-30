package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.spotlight.client.core.blueprints.commands.argument.types.StringOptionArgumentType;
import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.storage.InventoryStorage;
import net.minecraft.client.player.LocalPlayer;

import java.io.IOException;
import java.util.Set;

public class DeleteInventoryCommand extends ArgumentedCommand
{
    public DeleteInventoryCommand() throws IOException
    {
        super(new StringOptionArgumentType(() -> {
            try
            {
                return InventoryStorage.INSTANCE.getStoredNames();
            }
            catch (IOException e)
            {
                return Set.of();
            }
        }));
    }

    @Override
    public String getName()
    {
        return "idel";
    }

    @Override
    public String getDescription()
    {
        return "Delete a saved inventory";
    }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isNotNone()) return argsError;

        String name = args[0].toLowerCase();
        try
        {
            InventoryStorage.INSTANCE.remove(name);
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to remove inventory: " + e.getMessage());
        }

        return CommandFeedback.withSuccess("Removed inventory \"" + name + "\"");
    }
}

