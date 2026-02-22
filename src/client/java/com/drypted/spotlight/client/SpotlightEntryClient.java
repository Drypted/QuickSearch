package com.drypted.spotlight.client;

import com.drypted.spotlight.client.config.ModConfig;
import com.drypted.spotlight.client.core.handlers.SearchHandler;
import com.drypted.spotlight.client.gui.SpotlightScreen;
import com.drypted.spotlight.client.gui.utils.renderer.MosaicShader;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SpotlightEntryClient implements ClientModInitializer
{
    public static final String MOD_ID = "spotlight";
    public static final Logger LOGGER;
    private static ModConfig Config;

    public static final KeyMapping.Category KEY_CATEGORY_SPOTLIGHT;

    public static KeyMapping openSpotlightKeyMapping;
    public static KeyMapping closeSpotlightKeyMapping;

    public static KeyMapping openSpotlightCommandKeyMapping;

    static
    {
        LOGGER = LoggerFactory.getLogger(MOD_ID);
        KEY_CATEGORY_SPOTLIGHT = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keymappings"));
    }

    @Override
    public void onInitializeClient()
    {
        // initialize config
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        Config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        // register keybind
        openSpotlightKeyMapping = new KeyMapping(
                "key.spotlight.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KEY_CATEGORY_SPOTLIGHT
        );
        closeSpotlightKeyMapping = new KeyMapping(
                "key.spotlight.close",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_ESCAPE,
                KEY_CATEGORY_SPOTLIGHT
        );
        openSpotlightCommandKeyMapping = new KeyMapping(
                "key.spotlight.open_command",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                KEY_CATEGORY_SPOTLIGHT
        );

        KeyBindingHelper.registerKeyBinding(openSpotlightKeyMapping);
        KeyBindingHelper.registerKeyBinding(closeSpotlightKeyMapping);
        KeyBindingHelper.registerKeyBinding(openSpotlightCommandKeyMapping);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSpotlightKeyMapping.consumeClick())
            {
                SearchHandler.requestCreativeTabRebuild();
                client.setScreen(new SpotlightScreen(false));
            }
            while (openSpotlightCommandKeyMapping.consumeClick())
            {
                SearchHandler.requestCreativeTabRebuild();
                client.setScreen(new SpotlightScreen(true));
            }
        });

        // Resource reload listener for shaders
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener()
        {
            private final Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "shaders");

            @Override
            public Identifier getFabricId()
            {
                return id;
            }

            @Override
            public void onResourceManagerReload(@NonNull ResourceManager manager)
            {
                try
                {
                    // Safely reload mosaic shader from mod namespace
                    MosaicShader.free();
                    MosaicShader.load();
                    LOGGER.info("Mosaic shader reloaded successfully");
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to reload mosaic shader", e);
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register((minecraft) -> MosaicShader.free());
    }

    public static ModConfig getConfig()
    {
        return Config;
    }

    public static void saveConfig()
    {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }
}
