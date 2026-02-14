package com.drypted.spotlight.client.core.actions;

import net.minecraft.client.player.LocalPlayer;

/// Common utilities for actions
public class Action
{
    protected static boolean notInCreative(LocalPlayer player)
    {
        return !player.getAbilities().instabuild; // corresponds to creative mode
    }
}
