package com.drypted.spotlight.client.models;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class SearchResultData
{
    private final ItemStack icon;
    private final String name;
    private final ResourceLocation identifier;
    private final int maxStackSize;
    private final String commandString;

    public SearchResultData(ItemStack icon, String name, ResourceLocation identifier, int maxStackSize)
    {
        this.icon = icon;
        this.name = name;
        this.identifier = identifier;
        this.maxStackSize = maxStackSize;
        this.commandString = ("give @s " + identifier.toString() + " " + maxStackSize);
    }

    public SearchResultData(ItemStack icon, String name, ResourceLocation identifier, int maxStackSize, String commandString)
    {
        this.icon = icon;
        this.name = name;
        this.identifier = identifier;
        this.maxStackSize = maxStackSize;
        this.commandString = commandString;
    }

    public boolean isEmpty()
    {
        return this == EMPTY;
    }

    public boolean containsText(String text)
    {
        final String lowerText = text.toLowerCase();
        return name.toLowerCase().contains(lowerText) || identifier.getPath()
                                                                   .toLowerCase()
                                                                   .contains(lowerText);
    }

    public static SearchResultData fromBlock(Block block)
    {
        final ItemStack icon = block.asItem().getDefaultInstance();
        final String name = block.getName().getString();
        final ResourceLocation identifier = BuiltInRegistries.BLOCK.getKey(block);
        final int maxStackSize = block.asItem().getDefaultMaxStackSize();

        return new SearchResultData(icon, name, identifier, maxStackSize);
    }

    public static SearchResultData fromItem(Item item)
    {
        if (item == null || item == ItemStack.EMPTY.getItem() || item == Items.AIR)
            return SearchResultData.EMPTY;

        final ItemStack stack = item.getDefaultInstance();

        final String name = item.getName(stack).getString();
        final ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(item);
        final int maxStackSize = item.getDefaultMaxStackSize();

        return new SearchResultData(stack, name, identifier, maxStackSize);
    }

    public static SearchResultData fromItemStack(ItemStack stack)
    {
        if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR)
            return SearchResultData.EMPTY;

        final Item item = stack.getItem();
        final String name = item.getName(stack).getString();
        final ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(item);
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

    public ResourceLocation getIdentifier()
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
    public static final SearchResultData EMPTY = new SearchResultData(
            ItemStack.EMPTY,
            "",
            ResourceLocation.fromNamespaceAndPath("spotlight", "search_result_data_empty"),
            0
    );
}