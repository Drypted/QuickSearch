package com.drypted.spotlight.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

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
        this.commandString = buildGiveCommand(icon);
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

    private static String buildGiveCommand(ItemStack stack)
    {
        if (stack == null || stack == ItemStack.EMPTY || stack.getItem() == Items.AIR)
            return "give @s air 1";

        int count = stack.getMaxStackSize();

        ResourceKey<Item> itemKey = BuiltInRegistries.ITEM.getResourceKey(stack.getItem())
                                                          .orElseThrow();
        Holder<Item> holder = BuiltInRegistries.ITEM.getHolderOrThrow(itemKey);

        DataComponentPatch patch = stack.getComponentsPatch();
        ItemInput input = new ItemInput(holder, patch);

        Level level = Minecraft.getInstance().level;
        if (level == null)
        {
            return "give @s " + itemKey.location() + " " + count;
        }

        String itemPart = input.serialize(level.registryAccess());
        return "give @s " + itemPart + " " + count;
    }

    /* PUBLIC HELPERS */

    public boolean isEmpty()
    {
        return this == EMPTY;
    }

    public boolean containsText(String text)
    {
        final String lowerText = text.toLowerCase();
        return name.toLowerCase().contains(lowerText) || //
                identifier.getPath().toLowerCase().contains(lowerText);
    }

    /* GETTERS */

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

    /* PRE DEFINED */

    public static final SearchResultData EMPTY = new SearchResultData(
            ItemStack.EMPTY,
            "",
            ResourceLocation.fromNamespaceAndPath("spotlight", "search_result_data_empty"),
            0
    );
}