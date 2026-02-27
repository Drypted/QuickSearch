package com.drypted.spotlight.client.core.storage;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.google.gson.*;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class HotbarStorage
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotlightEntryClient.MOD_ID);
    private static final String FILE_NAME = "spotlight_hotbars.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getStorageFile()
    {
        return Minecraft.getInstance().gameDirectory.toPath().resolve(FILE_NAME);
    }

    public static void save(String name, Inventory inventory, RegistryAccess registryAccess) throws IOException
    {
        JsonObject root = readFile();

        Hotbar hotbar = new Hotbar();
        hotbar.storeFrom(inventory, registryAccess);

        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        JsonElement encoded = Hotbar.CODEC.encodeStart(ops, hotbar).getOrThrow();

        root.add(name.toLowerCase(), encoded);
        writeFile(root);
    }

    public static List<ItemStack> load(String name, HolderLookup.Provider provider) throws IOException
    {
        JsonObject root = readFile();
        JsonElement element = root.get(name.toLowerCase());
        if (element == null) return null;

        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;

        Hotbar hotbar = Hotbar.CODEC.parse(ops, element)
                .resultOrPartial(err -> LOGGER.warn("Failed to parse hotbar '{}': {}", name, err))
                .orElse(null);

        if (hotbar == null) return null;

        return hotbar.load(provider);
    }

    public static Set<String> getStoredNames() throws IOException
    {
        return readFile().keySet();
    }

    public static boolean exists(String name) throws IOException
    {
        return readFile().has(name.toLowerCase());
    }

    /* INTERNAL */

    private static JsonObject readFile() throws IOException
    {
        Path file = getStorageFile();
        if (!Files.exists(file)) return new JsonObject();

        try (Reader reader = Files.newBufferedReader(file))
        {
            JsonElement element = JsonParser.parseReader(reader);
            return element != null && element.isJsonObject()
                   ? element.getAsJsonObject()
                   : new JsonObject();
        }
    }

    private static void writeFile(JsonObject root) throws IOException
    {
        try (Writer writer = Files.newBufferedWriter(getStorageFile()))
        {
            GSON.toJson(root, writer);
        }
    }
}