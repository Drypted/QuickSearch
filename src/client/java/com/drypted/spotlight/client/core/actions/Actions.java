package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.models.SearchResultData;
import net.minecraft.client.player.LocalPlayer;

public class Actions
{
    public static void giveItem(LocalPlayer player, SearchResultData item)
    {
        if (player == null || item == null || item.getDefinition() == null)
            return;

        GiveItemAction.run(player, item.getDefinition(), item.getName(), item.getMaxStackSize());
    }

    public static void replaceHotbarItem(LocalPlayer player, SearchResultData item, int slot)
    {
        if (player == null || item == null || item.getDefinition() == null)
            return;

        ReplaceHotbarItemAction.run(
                player,
                item.getDefinition(),
                item.getName(),
                item.getMaxStackSize(),
                slot
        );
    }
}
