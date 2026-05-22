package com.drypted.quicksearch.client.core.storage;

import com.drypted.quicksearch.client.QuickSearchClient;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public abstract class PresetStorage<T>
{
    private static final String STORAGE_FOLDER = "quicksearch-storage/";

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickSearchClient.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DynamicOps<JsonElement> OPS = JsonOps.INSTANCE;

    /**
     * File name (not a full path) relative to the game directory, e.g. "quicksearch_hotbars.json".
     */
    protected abstract String getFileName();

    /**
     * Codec used to encode and decode values of type T.
     */
    protected abstract Codec<T> getCodec();

    /* PUBLIC API */

    /**
     * Encodes {@code value} and stores it under {@code name} (case-insensitive).
     *
     * @param name  the preset name
     * @param value the value to store
     *
     * @throws IOException              on file read/write failure
     * @throws IllegalArgumentException if encoding fails
     */
    public void save(String name, T value) throws IOException
    {
        JsonElement encoded = getCodec().encodeStart(OPS, value).getOrThrow();

        JsonObject root = readFile();
        root.add(name.toLowerCase(), encoded);
        writeFile(root);
    }

    /**
     * Loads and decodes the preset stored under {@code name} (case-insensitive).
     *
     * @param name the preset name
     *
     * @return the decoded value, or {@link Optional#empty()} if not found or decoding fails
     *
     * @throws IOException on file read failure
     */
    public Optional<T> load(String name) throws IOException
    {
        JsonObject root = readFile();
        JsonElement element = root.get(name.toLowerCase());
        if (element == null) return Optional.empty();

        return getCodec().parse(OPS, element)
                         .resultOrPartial(err -> LOGGER.warn(
                                 "[{}] Failed to parse preset '{}': {}",
                                 getFileName(),
                                 name,
                                 err
                         ));
    }

    /**
     * Removes the preset stored under {@code name} (case-insensitive). No-op if the name does not exist.
     *
     * @param name the preset name
     *
     * @throws IOException on file read/write failure
     */
    public void remove(String name) throws IOException
    {
        JsonObject root = readFile();
        if (root.remove(name.toLowerCase()) != null)
        {
            writeFile(root);
        }
    }

    /**
     * Returns all stored preset names.
     *
     * @throws IOException on file read failure
     */
    public Set<String> getStoredNames() throws IOException
    {
        return readFile().keySet();
    }

    /**
     * Returns whether a preset with the given name exists (case-insensitive).
     *
     * @param name the preset name
     *
     * @throws IOException on file read failure
     */
    public boolean exists(String name) throws IOException
    {
        return readFile().has(name.toLowerCase());
    }

    /* INTERNAL */

    private Path getStorageFile()
    {
        return Minecraft.getInstance().gameDirectory.toPath().resolve(STORAGE_FOLDER + getFileName());
    }

    private JsonObject readFile() throws IOException
    {
        Path file = getStorageFile();
        if (!Files.exists(file)) return new JsonObject();

        try (Reader reader = Files.newBufferedReader(file))
        {
            JsonElement element = JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject())
            {
                return element.getAsJsonObject();
            }
            LOGGER.warn("[{}] Storage file is not a valid JSON object, starting with empty storage", getFileName());
            return new JsonObject();
        }
    }

    private void writeFile(JsonObject root) throws IOException
    {
        Path file = getStorageFile();
        Files.createDirectories(file.getParent()); // ensure folder exists

        try (Writer writer = Files.newBufferedWriter(file))
        {
            GSON.toJson(root, writer);
        }
    }
}