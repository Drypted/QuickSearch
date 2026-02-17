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

    /// The width of the search bar
    @ConfigEntry.Gui.Tooltip
    public int searchBarWidth = 200;

    /// The height of the results box
    @ConfigEntry.Gui.Tooltip
    public int resultsBoxHeight = 100;

    /// Whether to show hotbar slots when the hotbar of spotlight is focused
    @ConfigEntry.Gui.Tooltip
    public boolean showHotbarSlots = true;

    /// Whether to show a message when item given or replaced
    @ConfigEntry.Gui.Tooltip
    public boolean showItemMessage = true;
}
