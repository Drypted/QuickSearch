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
    private final String itemDefinition;

    public SearchResultData(ItemStack icon, String name, ResourceLocation identifier)
    {
        this.icon = icon;
        this.name = name;
        this.identifier = identifier;
        this.maxStackSize = icon.getMaxStackSize();
        this.itemDefinition = buildItemDefinition(icon);
    }

    public static SearchResultData fromItem(Item item)
    {
        if (item == null || item == ItemStack.EMPTY.getItem() || item == Items.AIR)
            return SearchResultData.EMPTY;

        final ItemStack stack = item.getDefaultInstance();

        final String name = item.getName(stack).getString();
        final ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(item);

        return new SearchResultData(stack, name, identifier);
    }

    public static SearchResultData fromItemStack(ItemStack stack)
    {
        if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR)
            return SearchResultData.EMPTY;

        final Item item = stack.getItem();
        final String name = item.getName(stack).getString();
        final ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(item);

        return new SearchResultData(stack, name, identifier);
    }

    private static String buildItemDefinition(ItemStack stack)
    {
        if (stack == null || stack == ItemStack.EMPTY || stack.getItem() == Items.AIR)
            return "minecraft:air";

        ResourceKey<Item> itemKey = BuiltInRegistries.ITEM.getResourceKey(stack.getItem())
                                                          .orElseThrow();
        Holder<Item> holder = BuiltInRegistries.ITEM.getHolderOrThrow(itemKey);

        DataComponentPatch patch = stack.getComponentsPatch();
        ItemInput input = new ItemInput(holder, patch);

        Level level = Minecraft.getInstance().level;
        if (level == null)
        {
            return itemKey.location().toString();
        }

        // serialize the item input to a command string (e.g. "minecraft:diamond_sword[max_stack_size=1]")
        return input.serialize(level.registryAccess());
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

    public String getGiveCommand()
    {
        return String.format("/give @p %s %d", itemDefinition, maxStackSize);
    }

    public String getHotbarReplaceCommand(int hotbarSlot)
    {
        return String.format(
                "item replace entity @s hotbar.%d with %s %d",
                hotbarSlot,
                identifier,
                maxStackSize
        );
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

    public String getItemDefinition()
    {
        return itemDefinition;
    }

    public int getMaxStackSize()
    {
        return maxStackSize;
    }

    /* PRE DEFINED */

    public static final SearchResultData EMPTY = new SearchResultData(
            ItemStack.EMPTY,
            "",
            ResourceLocation.fromNamespaceAndPath("spotlight", "search_result_data_empty")
    );
}