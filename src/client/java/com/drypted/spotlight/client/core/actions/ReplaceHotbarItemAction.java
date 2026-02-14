package com.drypted.spotlight.client.core.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class ReplaceHotbarItemAction extends Action
{
    public static void run(LocalPlayer player, ItemInput item, String name, int maxStackSize, int slotIndex)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.gameMode == null)
            return;

        // Must be in creative mode
        if (notInCreative(player))
            return;

        ItemStack stack;
        try
        {
            stack = item.createItemStack(maxStackSize, false);
            // signature: createItemStack(int maxStackSize, boolean checkSize)
        }
        catch (CommandSyntaxException ignored)
        {
            return;
        }

        if (stack.isEmpty())
        {
            return;
        }

        int hotbarSlot = InventoryMenu.USE_ROW_SLOT_START + slotIndex; // hotbar slots are 36-44

        mc.gameMode.handleCreativeModeItemAdd(stack, hotbarSlot);

        player.displayClientMessage(
                Component.literal("Set slot " + (slotIndex + 1) + " to " + name),
                true
        );
    }
}
