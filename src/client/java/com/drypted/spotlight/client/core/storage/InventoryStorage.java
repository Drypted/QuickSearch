package com.drypted.spotlight.client.core.storage;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class InventoryStorage extends PresetStorage<NonNullList<ItemStack>>
{
    public static final InventoryStorage INSTANCE = new InventoryStorage();

    private InventoryStorage() { }

    @Override
    protected String getFileName()
    {
        return "inventories.json";
    }

    @Override
    protected Codec<NonNullList<ItemStack>> getCodec()
    {
        // ItemStack.CODEC list codec covering all 36 inventory slots.
        // Adjust the size if you want to include armor/offhand (up to 41).
        return ItemStack.OPTIONAL_CODEC.listOf().xmap(
                list -> {
                    NonNullList<ItemStack> result = NonNullList.withSize(36, ItemStack.EMPTY);
                    for (int i = 0; i < Math.min(list.size(), result.size()); i++)
                    {
                        result.set(i, list.get(i));
                    }
                    return result;
                }, List::copyOf
        );
    }

    public void saveFrom(String name, Inventory inventory) throws IOException
    {
        // items covers slots 0-35 (hotbar 0-8, main 9-35)
        save(name, inventory.getNonEquipmentItems());
    }

    public Optional<NonNullList<ItemStack>> loadStacks(String name) throws IOException
    {
        return load(name);
    }
}