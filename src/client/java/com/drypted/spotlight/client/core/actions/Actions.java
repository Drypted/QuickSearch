package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;

public class Actions
{
    public static void giveItem(LocalPlayer player, ItemsResultData item)
    {
        if (player == null || item == null || item.getDefinition() == null) return;

        GiveItemAction.run(player, item.getDefinition(), item.getName(), item.getMaxStackSize());
    }

    public static void replaceHotbarItem(LocalPlayer player, @Nullable ItemsResultData item, int slot)
    {
        if (player == null) return;
        if (item == null || item.getDefinition() == null) item = ItemsResultData.EMPTY;

        ReplaceHotbarItemAction.run(player, item.getDefinition(), item.getName(), item.getMaxStackSize(), slot);
    }
}
