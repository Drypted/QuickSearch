package com.drypted.spotlight.client.core.actions;

import com.drypted.spotlight.client.SpotlightClient;
import com.drypted.spotlight.client.core.blueprints.actions.Action;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ReplaceInventoryItemAction extends Action
{
    public static void run(LocalPlayer player, @Nullable ItemStack stack, String displayName, int slotIndex)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.gameMode == null)
        {
            handleError(player, ERROR.UNINITIALIZED);
            return;
        }

        if (notInCreative())
        {
            handleError(player, ERROR.NOT_IN_CREATIVE);
            return;
        }

        if (slotIndex < 0 || slotIndex > 35)
        {
            return;
        }

        if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR)
        {
            stack = ItemStack.EMPTY;
        }

        int containerSlot = slotIndex < 9
                ? InventoryMenu.USE_ROW_SLOT_START + slotIndex
                : InventoryMenu.INV_SLOT_START + (slotIndex - 9);

        mc.gameMode.handleCreativeModeItemAdd(stack, containerSlot);

        player.getInventory().setItem(slotIndex, stack);
        if (slotIndex < 9)
        {
            player.getInventory().getItem(slotIndex).setPopTime(5);
        }

        if (SpotlightClient.getConfig().notifications.showReplace)
        {
            String message = "Set inventory slot " + (slotIndex + 1) + " to " + displayName;
            if (displayName.isEmpty()) message = "Cleared inventory slot " + (slotIndex + 1);

            player.displayClientMessage(Component.literal(message), true);
        }

        final float volume = 0.5f;
        final float pitch = ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f;
        player.playSound(SoundEvents.ITEM_PICKUP, volume, pitch);
    }
}


