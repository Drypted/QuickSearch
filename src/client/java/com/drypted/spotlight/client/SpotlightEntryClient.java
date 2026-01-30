package com.drypted.spotlight.client;

import com.drypted.spotlight.client.core.SearchHandler;
import com.drypted.spotlight.client.gui.SpotlightScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotlightEntryClient implements ClientModInitializer
{
    public static final String MOD_ID = "spotlight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping openSpotlightKeyMapping;
    public static KeyMapping closeSpotlightKeyMapping;

    @Override
    public void onInitializeClient()
    {
        // register keybind
        openSpotlightKeyMapping = new KeyMapping(
                "key.spotlight.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KeyMapping.CATEGORY_MISC
        );
        closeSpotlightKeyMapping = new KeyMapping(
                "key.spotlight.close",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_ESCAPE,
                KeyMapping.CATEGORY_MISC
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSpotlightKeyMapping.consumeClick())
            {
                SearchHandler.requestCreativeTabRebuild();
                client.setScreen(new SpotlightScreen());
            }
        });
    }
}
