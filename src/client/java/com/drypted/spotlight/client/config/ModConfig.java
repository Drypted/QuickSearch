package com.drypted.spotlight.client.config;

import com.drypted.spotlight.client.SpotlightEntryClient;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = SpotlightEntryClient.MOD_ID)
public class ModConfig implements ConfigData
{
    /// Show help text in the hotbar when hotbar of spotlight is focused
    @ConfigEntry.Gui.Tooltip
    public boolean showHotbarHelpText = true;
}
