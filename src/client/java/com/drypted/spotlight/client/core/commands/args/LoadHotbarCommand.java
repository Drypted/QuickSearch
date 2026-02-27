package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.actions.ReplaceHotbarItemAction;
import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
import com.drypted.spotlight.client.core.storage.HotbarStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.util.List;

public class LoadHotbarCommand implements Command
{
    @Override
    public boolean requiresArgs()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "loadh";
    }

    @Override
    public String getDescription()
    {
        return "Load a saved hotbar combination";
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
        HolderLookup.Provider provider = player.level().registryAccess();

        List<ItemStack> stacks;
        try
        {
            stacks = HotbarStorage.load(name, provider);
        }
        catch (IOException e)
        {
            return CommandFeedback.withError("Failed to load hotbar: " + e.getMessage());
        }

        if (stacks == null) return CommandFeedback.withError("No hotbar preset found with name \"" + name + "\"");

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return CommandFeedback.withError("Game mode not available");

        for (int i = 0; i < stacks.size(); i++)
        {
            ItemStack stack = stacks.get(i);

            if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR)
            {
                // Clear the slot directly
                int hotbarSlot = InventoryMenu.USE_ROW_SLOT_START + i;
                mc.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, hotbarSlot);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                player.inventoryMenu.broadcastChanges();
                continue;
            }

            String itemName = stack.getItem().getName(stack).getString();
            ReplaceHotbarItemAction.run(player, buildItemInput(stack), itemName, stack.getCount(), i);
        }

        return CommandFeedback.withInfo("Loaded hotbar \"" + name + "\"");
    }

    private static ItemInput buildItemInput(ItemStack stack)
    {
        ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(stack.getItem()).orElseThrow();
        Holder<Item> holder = BuiltInRegistries.ITEM.getOrThrow(key);
        return new ItemInput(holder, stack.getComponentsPatch());
    }
}
