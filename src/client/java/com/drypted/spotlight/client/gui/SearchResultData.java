package com.drypted.spotlight.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class SearchResultData
{
    private final ItemStack icon;
    private final String name;
    private final String identifier;
    private final int maxStackSize;
    private final String commandString;

    public SearchResultData(ItemStack icon, String name, String identifier, int maxStackSize)
    {
        this.icon = icon;
        this.name = name;
        this.identifier = identifier;
        this.maxStackSize = maxStackSize;
        this.commandString = ("give @s " + identifier + " " + maxStackSize);
    }

    public SearchResultData(ItemStack icon, String name, String identifier, int maxStackSize, String commandString)
    {
        this.icon = icon;
        this.name = name;
        this.identifier = identifier;
        this.maxStackSize = maxStackSize;
        this.commandString = commandString;
    }

    public boolean isNotEmpty()
    {
        return this != EMPTY;
    }

    public boolean containsText(String text)
    {
        final String lowerText = text.toLowerCase();
        return name.toLowerCase().contains(lowerText) || identifier.toLowerCase()
                                                                   .contains(lowerText);
    }

    public static SearchResultData fromBlock(Block block)
    {
        final ItemStack icon = block.asItem().getDefaultInstance();
        final String name = block.getName().getString();
        final String identifier = BuiltInRegistries.BLOCK.getKey(block).toString();
        final int maxStackSize = block.asItem().getDefaultMaxStackSize();

        return new SearchResultData(icon, name, identifier, maxStackSize);
    }

    public static SearchResultData fromItem(Item item)
    {
        if (item == null || item == ItemStack.EMPTY.getItem() || item == Items.AIR)
            return SearchResultData.EMPTY;

        final ItemStack stack = item.getDefaultInstance();

        final String name = item.getName(stack).getString();
        final String identifier = BuiltInRegistries.ITEM.getKey(item).toString();
        final int maxStackSize = item.getDefaultMaxStackSize();

        return new SearchResultData(stack, name, identifier, maxStackSize);
    }

    public static SearchResultData fromItemStack(ItemStack stack)
    {
        if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR)
            return SearchResultData.EMPTY;

        final Item item = stack.getItem();
        final String name = item.getName(stack).getString();
        final String identifier = BuiltInRegistries.ITEM.getKey(item).toString();
        final int maxStackSize = item.getDefaultMaxStackSize();

        return new SearchResultData(stack, name, identifier, maxStackSize);
    }

    /* GETTERS & SETTERS */

    public ItemStack getIcon()
    {
        return icon;
    }

    public String getName()
    {
        return name;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String getCommandString()
    {
        return commandString;
    }

    public int getMaxStackSize()
    {
        return maxStackSize;
    }

    /* Predefined */

    public static final SearchResultData EMPTY = new SearchResultData(ItemStack.EMPTY, "", "", 0);
}