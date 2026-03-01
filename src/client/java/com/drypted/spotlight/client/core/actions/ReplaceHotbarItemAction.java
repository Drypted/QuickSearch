package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.SpotlightEntryClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ReplaceHotbarItemAction extends Action
{
    public static void run(LocalPlayer player, @Nullable ItemInput item, String displayName, int maxStackSize, int slotIndex)
    {
        ItemStack stack = null;
        try
        {
            // signature: createItemStack(int maxStackSize, boolean checkSize)
            if (item != null) stack = item.createItemStack(maxStackSize, false);
        }
        catch (Exception ignored)
        {
        }

        ReplaceHotbarItemAction.run(player, stack, displayName, slotIndex);
    }


    public static void run(LocalPlayer player, @Nullable ItemStack stack, String displayName, int slotIndex)
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

        if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR)
        {
            // replace with empty slot
            stack = ItemStack.EMPTY;
        }

        int hotbarSlot = InventoryMenu.USE_ROW_SLOT_START + slotIndex; // hotbar slots are 36-44

        mc.gameMode.handleCreativeModeItemAdd(stack, hotbarSlot);

        player.getInventory().setItem(slotIndex, stack);
        player.getInventory().getItem(slotIndex).setPopTime(5); // item pickup animation

        if (SpotlightEntryClient.getConfig().showItemMessage)
        {
            String message = "Set slot " + (slotIndex + 1) + " to " + displayName;
            if (displayName.isEmpty()) message = "Cleared slot " + (slotIndex + 1);

            player.displayClientMessage(Component.literal(message), true);
        }

        final float volume = 0.5f;
        final float pitch = ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f;
        player.playSound(SoundEvents.ITEM_PICKUP, volume, pitch);
    }
}
