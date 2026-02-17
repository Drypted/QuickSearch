package com.drypted.spotlight.client;

import com.drypted.spotlight.client.config.ModConfig;
import com.drypted.spotlight.client.core.handlers.SearchHandler;
import com.drypted.spotlight.client.gui.SpotlightScreen;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotlightEntryClient implements ClientModInitializer
{
    public static final String MOD_ID = "spotlight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig Config;

    public static final String KEY_CATEGORY_SPOTLIGHT = "key.categories.spotlight";
    public static KeyMapping openSpotlightKeyMapping;
    public static KeyMapping closeSpotlightKeyMapping;

    public static KeyMapping openSpotlightCommandKeyMapping;

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
