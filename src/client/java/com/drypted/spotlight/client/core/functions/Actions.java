package com.drypted.spotlight.client.core.functions;

import com.drypted.spotlight.client.models.SearchResultData;
import net.minecraft.client.player.LocalPlayer;

public class Actions
{
    public static void giveItem(LocalPlayer player, SearchResultData item)
    {
        if (player == null || item == null || item.getDefinition() == null)
            return;

        player.connection.sendCommand(item.getGiveCommand());
        // GiveItemAction.run(player, item.getItemDefinition(), item.getMaxStackSize());
    }

    public static void replaceHotbarItem(LocalPlayer player, SearchResultData item, int slot)
    {
        if (player == null || item == null || item.getDefinition() == null)
            return;

        player.connection.sendCommand(item.getHotbarReplaceCommand(slot));
        // ReplaceHotbarItemAction.run(
        //         player,
        //         item.getItemDefinition(),
        //         item.getMaxStackSize(),
        //         slot
        // );
    }
}
