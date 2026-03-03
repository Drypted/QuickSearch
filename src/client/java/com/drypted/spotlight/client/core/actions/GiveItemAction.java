package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import com.drypted.spotlight.client.core.blueprints.actions.Action;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class GiveItemAction extends Action
{
    public static void run(LocalPlayer player, ItemsResultData item)
    {
        if (player == null || item == null || item.getDefinition() == null) return;

        GiveItemAction.run(player, item.getDefinition(), item.getName(), item.getMaxStackSize());
    }

    public static void run(LocalPlayer player, ItemInput item, String name, int maxStackSize)
    {
        if (player == null || item == null) return;

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

        GiveItemAction.run(player, stack, name);
    }

    public static void run(LocalPlayer player, ItemStack stack, String name)
    {
        if (player == null || stack == null) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.gameMode == null)
        {
            handleError(player, ERROR.UNINITIALIZED);
            return;
        }

        if (stack.isEmpty())
        {
            handleError(player, ERROR.INVALID_ITEM);
            return;
        }

        // Must be in creative mode
        if (notInCreative())
        {
            handleError(player, ERROR.NOT_IN_CREATIVE);
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

        // update client
        if (containerSlot != -1) // -1 drops item
        {
            player.getInventory().setItem(invSlot, stack);
            if (invSlot < 9) // show animation if item in hotbar only
                player.getInventory().getItem(invSlot).setPopTime(5); // item pickup animation
        }

        // Feedback
        if (SpotlightEntryClient.getConfig().notifications.showItemMessage)
            player.displayClientMessage(Component.literal("Gave " + name), true);

        final float volume = 0.5f;
        final float pitch = ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f;
        player.playSound(SoundEvents.ITEM_PICKUP, volume, pitch);
    }
}
