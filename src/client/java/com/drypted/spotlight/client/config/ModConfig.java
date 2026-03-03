package com.drypted.spotlight.client.config;

import com.drypted.spotlight.client.SpotlightEntryClient;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = SpotlightEntryClient.MOD_ID)
public class ModConfig implements ConfigData
{
    @ConfigEntry.Gui.Tooltip public boolean enableSpotlight = true;

    @ConfigEntry.Gui.CollapsibleObject public UI ui = new UI();
    @ConfigEntry.Gui.CollapsibleObject public Hotbar hotbar = new Hotbar();
    @ConfigEntry.Gui.CollapsibleObject public Search search = new Search();
    @ConfigEntry.Gui.CollapsibleObject public Notifications notifications = new Notifications();

    public static class UI implements ConfigData
    {

        @ConfigEntry.BoundedDiscrete(min = 100, max = 800)
        @ConfigEntry.Gui.Tooltip
        public int searchBarWidth = 200;
        @ConfigEntry.BoundedDiscrete(min = 50, max = 400)
        @ConfigEntry.Gui.Tooltip
        public int resultsBoxHeight = 100;
    }

    public static class Hotbar implements ConfigData
    {
        @ConfigEntry.Gui.Tooltip public boolean showHotbarHelpText = true;
        @ConfigEntry.Gui.Tooltip public boolean showHotbarSlots = true;
    }

    public static class Search implements ConfigData
    {
        @ConfigEntry.Gui.Tooltip public boolean fuzzySearch = true;
        @ConfigEntry.Gui.Tooltip public boolean caseSensitiveSearch = false;
        @ConfigEntry.Gui.Tooltip public boolean rememberLastQuery = true;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 200)
        @ConfigEntry.Gui.Tooltip
        public int maxResults = 50;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip(count = 1)
        public CompletionType completionType = CompletionType.SINGLE_WORD;
    }

    public static class Notifications implements ConfigData
    {
        @ConfigEntry.Gui.Tooltip public boolean showItemMessage = true;
        @ConfigEntry.Gui.Tooltip public boolean showGiveNotifications = true;
    }

    public enum CompletionType
    {
        NONE,
        SINGLE_WORD,
        WHOLE_QUERY
    }
}