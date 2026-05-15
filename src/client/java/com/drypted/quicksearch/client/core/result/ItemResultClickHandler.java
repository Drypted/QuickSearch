package com.drypted.quicksearch.client.core.result;

import com.drypted.quicksearch.client.core.actions.GiveItemAction;
import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ItemResultClickHandler
{
    public void onItemClicked(ItemsResultData data)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) GiveItemAction.run(player, data);
    }
}
