package com.drypted.quicksearch.client.core.storage;

import com.mojang.serialization.Codec;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class HotbarStorage extends PresetStorage<Hotbar>
{
    public static final HotbarStorage INSTANCE = new HotbarStorage();

    private HotbarStorage() { }

    @Override
    protected String getFileName()
    {
        return "hotbars.json";
    }

    @Override
    protected Codec<Hotbar> getCodec()
    {
        return Hotbar.CODEC;
    }

    public void saveFrom(String name, Inventory inventory, RegistryAccess registryAccess) throws IOException
    {
        Hotbar hotbar = new Hotbar();
        hotbar.storeFrom(inventory, registryAccess);
        save(name, hotbar);
    }

    public Optional<List<ItemStack>> loadStacks(String name, HolderLookup.Provider provider) throws IOException
    {
        return load(name).map(hotbar -> hotbar.load(provider));
    }
}