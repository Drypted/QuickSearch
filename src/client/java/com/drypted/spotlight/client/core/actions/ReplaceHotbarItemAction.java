package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class ReplaceHotbarItemAction extends Action
{
    public static void run(LocalPlayer player, ItemInput item, String name, int maxStackSize, int slotIndex)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.gameMode == null)
        {
            handleError(player, ERROR.UNINITIALIZED);
            return;
        }

        // Must be in creative mode
        if (notInCreative())
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

        int hotbarSlot = InventoryMenu.USE_ROW_SLOT_START + slotIndex; // hotbar slots are 36-44

        mc.gameMode.handleCreativeModeItemAdd(stack, hotbarSlot);

        player.getInventory().setItem(slotIndex, stack);
        player.getInventory().getItem(slotIndex).setPopTime(5); // item pickup animation

        if (SpotlightEntryClient.getConfig().showItemMessage)
            player.displayClientMessage(Component.literal("Set slot " + (slotIndex + 1) + " to " + name), true);

        final float volume = 0.5f;
        final float pitch = ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f;
        player.playSound(SoundEvents.ITEM_PICKUP, volume, pitch);
    }
}
