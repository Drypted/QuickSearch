package com.drypted.quicksearch.client.core.commands.args;

import com.drypted.quicksearch.client.core.actions.ReplaceInventoryItemAction;
import com.drypted.quicksearch.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.quicksearch.client.core.blueprints.commands.argument.types.StringOptionArgumentType;
import com.drypted.quicksearch.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.quicksearch.client.core.storage.InventoryStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LoadInventoryCommand extends ArgumentedCommand
{
    public LoadInventoryCommand() throws IOException
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
    public String getName() { return "iload"; }

    @Override
    public String getDescription() { return "Load a saved inventory"; }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isNotNone()) return argsError;

        String name = args[0].toLowerCase();

        Optional<NonNullList<ItemStack>> result;
        try
        {
            result = InventoryStorage.INSTANCE.loadStacks(name);
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to load inventory: " + e.getMessage());
        }

        if (result.isEmpty()) return CommandFeedback.withError("No inventory preset found with name \"" + name + "\"");

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return CommandFeedback.withError("Game mode not available");

        List<ItemStack> stacks = result.get();
        for (int i = 0; i < stacks.size(); i++)
        {
            ItemStack stack = stacks.get(i);

            String itemName = "";
            if (stack != null && !stack.isEmpty() && stack.getItem() != Items.AIR)
            {
                itemName = stack.getHoverName().getString();
            }

            ReplaceInventoryItemAction.run(player, stack, itemName, i);
        }

        return CommandFeedback.withSuccess("Loaded inventory \"" + name + "\"");
    }
}
