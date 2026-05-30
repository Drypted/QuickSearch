package com.drypted.quicksearch.client.init;

import com.drypted.quicksearch.client.QuickSearchClient;
import com.drypted.quicksearch.client.core.handlers.SearchHandler;
import com.drypted.quicksearch.client.ui.QuickSearchScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.drypted.quicksearch.client.QuickSearchClient.MOD_ID;

public class ModKeybinds
{
    private static final KeyMapping.Category QUICK_SEARCH_KEY_CATEGORY;

    private static KeyMapping openKey;
    private static KeyMapping closeKey;
    private static KeyMapping openCommandKey;
    private static KeyMapping focusHotbarKey;

    // input
    private static KeyMapping inputSubmitKey;

    static
    {
        QUICK_SEARCH_KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(
                MOD_ID,
                "keymappings"
        ));
    }

    public static void register()
    {
        // register keybind
        openKey = getKeyMapping("open", GLFW.GLFW_KEY_Y);
        closeKey = getKeyMapping("close", GLFW.GLFW_KEY_ESCAPE);
        openCommandKey = getKeyMapping("open_command", GLFW.GLFW_KEY_U);
        focusHotbarKey = getKeyMapping("focus_hotbar", GLFW.GLFW_KEY_GRAVE_ACCENT);
        inputSubmitKey = getKeyMapping("input_submit", GLFW.GLFW_KEY_ENTER);

        KeyBindingHelper.registerKeyBinding(openKey);
        KeyBindingHelper.registerKeyBinding(closeKey);
        KeyBindingHelper.registerKeyBinding(openCommandKey);
        KeyBindingHelper.registerKeyBinding(focusHotbarKey);
        KeyBindingHelper.registerKeyBinding(inputSubmitKey);
    }

    private static KeyMapping getKeyMapping(String id, int glfwKey)
    {
        return new KeyMapping(
                "key.quicksearch." + id,
                InputConstants.Type.KEYSYM,
                glfwKey,
                QUICK_SEARCH_KEY_CATEGORY
        );
    }

    public static void registerClientCallback()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!QuickSearchClient.getConfig().enabled) return;

            while (openKey.consumeClick())
            {
                SearchHandler.requestCreativeTabRebuild();
                client.setScreen(new QuickSearchScreen(false));
            }
            while (openCommandKey.consumeClick())
            {
                SearchHandler.requestCreativeTabRebuild();
                client.setScreen(new QuickSearchScreen(true));
            }
        });
    }

    /* GETTERS & SETTERS */

    public static KeyMapping.Category getQuickSearchKeyCategory() { return QUICK_SEARCH_KEY_CATEGORY; }

    public static KeyMapping getCloseKey() { return closeKey; }

    public static KeyMapping getOpenKey() { return openKey; }

    public static KeyMapping getOpenCommandKey() { return openCommandKey; }

    public static KeyMapping getFocusHotbarKey() { return focusHotbarKey; }

    public static KeyMapping getInputSubmitKey() { return inputSubmitKey; }
}
