package com.drypted.spotlight.client.core.handlers;

import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

final class ItemIndexBuilder
{
    private ItemIndexBuilder() { }

    static List<ItemsResultData> buildGameItems()
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
        {
            return Collections.emptyList();
        }

        LinkedHashMap<String, ItemsResultData> combined = new LinkedHashMap<>();

        // Creative tab items are shown first to preserve user-facing ordering.
        CreativeModeTabs.allTabs().stream().flatMap(tab -> tab.getDisplayItems().stream()).forEach(stack -> {
            ItemsResultData data = ItemsResultData.fromItemStack(stack);
            combined.putIfAbsent(data.getSerializedDefinition(), data);
        });

        // Registry fallback includes hidden or non-tab-exposed items.
        BuiltInRegistries.ITEM.stream().forEach(item -> {
            try
            {
                ItemsResultData data = ItemsResultData.fromItem(item);
                combined.putIfAbsent(data.getSerializedDefinition(), data);
            }
            catch (Exception ignored)
            {
                // Some items cannot be safely materialized as stacks.
            }
        });

        return List.copyOf(combined.values());
    }
}