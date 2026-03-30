package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.actions.ReplaceHotbarItemAction;
import com.drypted.spotlight.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.spotlight.client.core.blueprints.commands.argument.types.StringOptionArgumentType;
import com.drypted.spotlight.client.core.blueprints.feedback.CommandFeedback;
import com.drypted.spotlight.client.core.storage.HotbarStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LoadHotbarCommand extends ArgumentedCommand
{
    public LoadHotbarCommand() throws IOException
    {
        super(new StringOptionArgumentType(() -> {
            try
            {
                return HotbarStorage.INSTANCE.getStoredNames();
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
        return "hload";
    }

    @Override
    public String getDescription()
    {
        return "Load a saved hotbar combination";
    }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isNotNone()) return argsError;

        String name = args[0].toLowerCase();
        HolderLookup.Provider provider = player.level().registryAccess();

        Optional<List<ItemStack>> result;
        try
        {
            result = HotbarStorage.INSTANCE.loadStacks(name, provider);
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to load hotbar: " + e.getMessage());
        }

        if (result.isEmpty()) return CommandFeedback.withError("No hotbar preset found with name \"" + name + "\"");

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

            ReplaceHotbarItemAction.run(player, stack, itemName, i);
        }

        return CommandFeedback.withSuccess("Loaded hotbar \"" + name + "\"");
    }
}
