package com.drypted.spotlight.client.init;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.handlers.SearchHandler;
import com.drypted.spotlight.client.ui.SpotlightScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.drypted.spotlight.client.SpotlightEntryClient.MOD_ID;

public class ModKeybinds
{
    private static final KeyMapping.Category SPOTLIGHT_KEY_CATEGORY;

    private static KeyMapping openSpotlightKeyMapping;
    private static KeyMapping closeSpotlightKeyMapping;
    private static KeyMapping openSpotlightCommandKeyMapping;

    static
    {
        SPOTLIGHT_KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keymappings"));
    }

    public static void register()
    {
        // register keybind
        openSpotlightKeyMapping = new KeyMapping(
                "key.spotlight.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                SPOTLIGHT_KEY_CATEGORY
        );
        closeSpotlightKeyMapping = new KeyMapping(
                "key.spotlight.close",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_ESCAPE,
                SPOTLIGHT_KEY_CATEGORY
        );
        openSpotlightCommandKeyMapping = new KeyMapping(
                "key.spotlight.open_command",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                SPOTLIGHT_KEY_CATEGORY
        );

        KeyBindingHelper.registerKeyBinding(openSpotlightKeyMapping);
        KeyBindingHelper.registerKeyBinding(closeSpotlightKeyMapping);
        KeyBindingHelper.registerKeyBinding(openSpotlightCommandKeyMapping);
    }

    public static void registerClientCallback()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!SpotlightEntryClient.getConfig().enableSpotlight) return;

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

    /* GETTERS & SETTERS */

    public static KeyMapping.Category getSpotlightKeyCategory()
    {
        return SPOTLIGHT_KEY_CATEGORY;
    }

    public static KeyMapping getCloseSpotlightKey()
    {
        return closeSpotlightKeyMapping;
    }

    public static KeyMapping getOpenSpotlightKey()
    {
        return openSpotlightKeyMapping;
    }

    public static KeyMapping getOpenSpotlightCommandKey()
    {
        return openSpotlightCommandKeyMapping;
    }
}
