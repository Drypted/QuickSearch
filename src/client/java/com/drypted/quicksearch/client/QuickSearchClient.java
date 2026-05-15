package com.drypted.quicksearch.client;

import com.drypted.quicksearch.client.config.ModConfig;
import com.drypted.quicksearch.client.init.ModKeybinds;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickSearchClient implements ClientModInitializer
{
    public static final String MOD_ID = "quicksearch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig Config;

    @Override
    public void onInitializeClient()
    {
        // initialize config
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        Config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ModKeybinds.register();
        ModKeybinds.registerClientCallback();
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
