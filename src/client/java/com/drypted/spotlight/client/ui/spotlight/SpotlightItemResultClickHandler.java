package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.actions.GiveItemAction;
import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class SpotlightItemResultClickHandler
{
    public void onItemClicked(ItemsResultData data)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) GiveItemAction.run(player, data);
    }
}
