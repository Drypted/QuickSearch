package com.drypted.quicksearch.client.core.commands.args;

import com.drypted.quicksearch.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.quicksearch.client.core.blueprints.commands.argument.types.WordArgumentType;
import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.quicksearch.client.core.storage.InventoryStorage;
import net.minecraft.client.player.LocalPlayer;

import java.io.IOException;

public class SaveInventoryCommand extends ArgumentedCommand
{
    public SaveInventoryCommand()
    {
        super(new WordArgumentType("<name>"));
    }

    @Override
    public String getName() { return "isave"; }

    @Override
    public String getDescription() { return "Save the current inventory"; }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isNotNone()) return argsError;

        String name = args[0].toLowerCase();

        try
        {
            InventoryStorage.INSTANCE.saveFrom(name, player.getInventory());
            return CommandFeedback.withSuccess("Saved inventory as \"" + name + "\"");
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to save inventory: " + e.getMessage());
        }
    }
}
