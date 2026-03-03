package com.drypted.spotlight.client.core.blueprints.actions;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/// Common utilities for actions
public class Action
{
    protected static boolean notInCreative()
    {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        return gameMode != null && !gameMode.getPlayerMode().isCreative(); // corresponds to creative mode
    }

    protected static void showErrorMessage(LocalPlayer player, String text)
    {
        player.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.RED), true);
    }

    protected static boolean validate(LocalPlayer player, ItemStack stack)
    {
        if (player == null) return false;

        if (notInCreative())
        {
            handleError(player, ERROR.NOT_IN_CREATIVE);
            return false;
        }
        if (stack == null || stack.isEmpty())
        {
            handleError(player, ERROR.INVALID_ITEM);
            return false;
        }
        return true;
    }

    protected static void handleError(LocalPlayer player, Action.ERROR error)
    {
        switch (error)
        {
            case NONE:
            case UNINITIALIZED:
                return;
            case NOT_IN_CREATIVE:
                showErrorMessage(player, "Please use creative mode");
                return;
            case INVALID_ITEM:
                showErrorMessage(player, "Invalid Item");
                return;
        }
    }

    protected enum ERROR
    {
        NONE,
        UNINITIALIZED,
        NOT_IN_CREATIVE,
        INVALID_ITEM
    }
}
