package com.drypted.spotlight.client.core.command;

import net.minecraft.client.player.LocalPlayer;

public interface Command
{
    String getName();

    String getDescription();

    void execute(String[] args, LocalPlayer player);
}
