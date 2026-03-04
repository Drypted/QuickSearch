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

    private static KeyMapping openSpotlightKey;
    private static KeyMapping closeSpotlightKey;
    private static KeyMapping openSpotlightCommandKey;

    // input
    private static KeyMapping inputSubmitKey;

    static
    {
        SPOTLIGHT_KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keymappings"));
    }

    public static void register()
    {
        // register keybind
        openSpotlightKey = getKeyMapping("open", GLFW.GLFW_KEY_Y);
        closeSpotlightKey = getKeyMapping("close", GLFW.GLFW_KEY_ESCAPE);
        openSpotlightCommandKey = getKeyMapping("open_command", GLFW.GLFW_KEY_U);
        inputSubmitKey = getKeyMapping("input_submit", GLFW.GLFW_KEY_ENTER);

        KeyBindingHelper.registerKeyBinding(openSpotlightKey);
        KeyBindingHelper.registerKeyBinding(closeSpotlightKey);
        KeyBindingHelper.registerKeyBinding(openSpotlightCommandKey);
        KeyBindingHelper.registerKeyBinding(inputSubmitKey);
    }

    private static KeyMapping getKeyMapping(String id, int glfwKey)
    {
        return new KeyMapping("key.spotlight." + id, InputConstants.Type.KEYSYM, glfwKey, SPOTLIGHT_KEY_CATEGORY);
    }

    public static void registerClientCallback()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!SpotlightEntryClient.getConfig().enableSpotlight) return;

            while (openSpotlightKey.consumeClick())
            {
                SearchHandler.requestCreativeTabRebuild();
                client.setScreen(new SpotlightScreen(false));
            }
            while (openSpotlightCommandKey.consumeClick())
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
        return closeSpotlightKey;
    }

    public static KeyMapping getOpenSpotlightKey()
    {
        return openSpotlightKey;
    }

    public static KeyMapping getOpenSpotlightCommandKey()
    {
        return openSpotlightCommandKey;
    }

    public static KeyMapping getInputSubmitKey()
    {
        return inputSubmitKey;
    }
}
