package com.drypted.spotlight.client.core.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class GiveItemAction extends Action
{
    public static void run(LocalPlayer player, ItemInput item, String name, int maxStackSize)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.gameMode == null)
        {
            handleError(player, ERROR.UNINITIALIZED);
            return;
        }

        // Must be in creative mode
        if (notInCreative(player))
        {
            handleError(player, ERROR.NOT_IN_CREATIVE);
            return;
        }

        ItemStack stack;
        try
        {
            stack = item.createItemStack(maxStackSize, false);
            // signature: createItemStack(int maxStackSize, boolean checkSize)
        }
        catch (CommandSyntaxException ignored)
        {
            handleError(player, ERROR.INVALID_ITEM);
            return;
        }

        if (stack.isEmpty())
        {
            handleError(player, ERROR.INVALID_ITEM);
            return;
        }

        MultiPlayerGameMode gameMode = mc.gameMode;

        int invSlot = player.getInventory().getFreeSlot();
        // returns -1 for none, 0-8 for hotbar, 9-35 for main inventory

        int containerSlot;

        if (invSlot == -1) // no slot, drop
            containerSlot = -1;
        else if (invSlot < 9) // in hotbar
            containerSlot = InventoryMenu.USE_ROW_SLOT_START + invSlot;
        else // in main inventory
            containerSlot = InventoryMenu.INV_SLOT_START + (invSlot - 9);

        // if no empty slot was found, then handleCreativeModeItemAdd will drop the item
        gameMode.handleCreativeModeItemAdd(stack, containerSlot);

        // Client Message
        // TODO: add config
        player.displayClientMessage(Component.literal("Gave " + name), true);
    }
}
