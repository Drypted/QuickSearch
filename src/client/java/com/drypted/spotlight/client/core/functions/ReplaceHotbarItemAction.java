package com.drypted.spotlight.client.core.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ReplaceHotbarItemAction
{
    /// Doesn't sync changes to client, use a appropriate packet to inform server
    public static void run(Player player, ItemInput item, int maxStackSize, int slotIndex)
    {
        ItemStack stack = null;
        try
        {
            stack = item.createItemStack(maxStackSize, false);
            // signature: createItemStack(int maxStackSize, boolean checkSize)
        }
        catch (CommandSyntaxException ignored)
        {
        }

        if (stack == null || stack.isEmpty())
        {
            return;
        }

        // 2. Add to inventory directly (Silent)
        // This method doesn't trigger log messages
        player.getInventory().setItem(slotIndex, stack);

        // 3. Sync changes to the client
        // (Important for servers so the player sees the item immediately)
        player.containerMenu.broadcastChanges();

        // Optional: Send a private message only to the player
        // This does NOT show up in server logs
        player.displayClientMessage(Component.literal("You replaced an item silently."), true);
    }
}
