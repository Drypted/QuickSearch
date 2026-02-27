package com.drypted.spotlight.client.models;

import com.drypted.spotlight.client.core.search.Searchable;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class ItemsResultData implements Searchable
{
    private final ItemStack icon;
    private final String name;
    private final Identifier identifier;
    private final int maxStackSize;
    private final ItemInput definition;

    public ItemsResultData(ItemStack icon, String name, Identifier identifier)
    {
        this.icon = icon;
        this.name = name;
        this.identifier = identifier;
        this.maxStackSize = icon.getMaxStackSize();
        this.definition = buildItemInput(icon);
    }

    public static ItemsResultData fromItem(Item item)
    {
        if (item == null || item == ItemStack.EMPTY.getItem() || item == Items.AIR) return ItemsResultData.EMPTY;

        final ItemStack stack = item.getDefaultInstance();

        final String name = item.getName(stack).getString();
        final Identifier identifier = BuiltInRegistries.ITEM.getKey(item);

        return new ItemsResultData(stack, name, identifier);
    }

    public static ItemsResultData fromItemStack(ItemStack stack)
    {
        if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR) return ItemsResultData.EMPTY;

        final Item item = stack.getItem();
        final String name = item.getName(stack).getString();
        final Identifier identifier = BuiltInRegistries.ITEM.getKey(item);

        return new ItemsResultData(stack, name, identifier);
    }

    private static ItemInput buildItemInput(ItemStack stack)
    {
        if (stack == null || stack == ItemStack.EMPTY || stack.getItem() == Items.AIR) return null;

        ResourceKey<Item> itemKey = BuiltInRegistries.ITEM.getResourceKey(stack.getItem()).orElseThrow();
        Holder<Item> holder = BuiltInRegistries.ITEM.getOrThrow(itemKey);

        DataComponentPatch patch = stack.getComponentsPatch();
        return new ItemInput(holder, patch);
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
        return String.format("give @p %s %d", getSerializedDefinition(), maxStackSize);
    }

    public String getHotbarReplaceCommand(int hotbarSlot)
    {
        return String.format(
                "item replace entity @s hotbar.%d with %s %d",
                hotbarSlot,
                getSerializedDefinition(),
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

    public Identifier getIdentifier()
    {
        return identifier;
    }

    public ItemInput getDefinition()
    {
        return definition;
    }

    public int getMaxStackSize()
    {
        return maxStackSize;
    }

    public String getSerializedDefinition()
    {
        Level level = Minecraft.getInstance().level;
        if (level == null)
        {
            return "";
        }

        return definition == null ? "" : definition.serialize(level.registryAccess());
    }

    /* PRE DEFINED */

    public static final ItemsResultData EMPTY = new ItemsResultData(
            ItemStack.EMPTY,
            "",
            Identifier.fromNamespaceAndPath("spotlight", "search_result_data_empty")
    );

    /* SEARCHABLE IMPLEMENTATION */
    @Override
    public String getPrimaryQuery()
    {
        return this.getName();
    }

    @Override
    public String getSecondaryQuery()
    {
        return this.getIdentifier().getPath().toLowerCase();
    }
}