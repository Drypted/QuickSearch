package com.drypted.spotlight.client.gui.components;

import com.drypted.spotlight.client.core.models.SearchResultData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Consumer;

public class HotbarWidget extends AbstractWidget
{
    private static final int HOTBAR_SLOT_PADDING = 2;
    private static final int HOTBAR_SLOTS = 9;

    public final ArrayList<SearchHotbarWidget> searchHotbarWidgets = new ArrayList<>(HOTBAR_SLOTS);
    private SearchHotbarWidget selectedHotbarWidget = null;

    private HotbarHelpText hotbarInstructionText = HotbarHelpText.UNSELECTED;

    private Consumer<Boolean> onFocusChanged;

    public HotbarWidget(int searchBarWidth, int searchBarX, int searchBarY, SearchInputWidget searchInputWidget)
    {
        super(0, 0, 0, 0, Component.empty());
        final float iconSize = (searchBarWidth - HOTBAR_SLOT_PADDING * (HOTBAR_SLOTS + 1)) / (float) HOTBAR_SLOTS;
        final int endY = searchBarY - searchInputWidget.getOutlineThickness() - HOTBAR_SLOT_PADDING;
        final int startY = (int) Math.ceil(endY - iconSize);
        float cursor = searchBarX + HOTBAR_SLOT_PADDING;

        for (int i = 0; i < HOTBAR_SLOTS; i++)
        {
            final SearchHotbarWidget hotbarWidget = SearchHotbarWidget.builder(
                    i, //
                    (int) Math.ceil(cursor),
                    startY,
                    (int) Math.ceil(iconSize),
                    (int) Math.ceil(iconSize)
            ).build();
            hotbarWidget.setOnClickCallback(mouseButtonClick -> {
                SearchResultData item = hotbarWidget.getSearchResultData();
                if (item == null || item.isEmpty())
                    return;

                onHotbarKeyPressed(hotbarWidget, 0);
            });
            this.searchHotbarWidgets.add(hotbarWidget);
            cursor += iconSize + HOTBAR_SLOT_PADDING;
        }

        this.setOnFocusChanged((focused) -> {
            if (!focused)
                this.selectedHotbarWidget = null;
        });
    }

    public static HotbarWidget create(int searchBarWidth, int searchBarX, int searchBarY, SearchInputWidget searchInputWidget)
    {
        return new HotbarWidget(
                searchBarWidth,     //
                searchBarX,         //
                searchBarY,         //
                searchInputWidget   //
        );
    }

    /* RENDERING */

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        searchHotbarWidgets.forEach(widget -> widget.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        ));
    }

    /* HOTBAR */

    public void highlightSlot(int slotIndex)
    {
        searchHotbarWidgets.forEach(widget -> widget.setHighlighted(false));
        // get slot index and set show bind to true for that widget
        searchHotbarWidgets.stream()
                           .filter(widget -> widget.getHotbarIndex() == slotIndex)
                           .findFirst()
                           .ifPresent(widget -> widget.setHighlighted(true));
    }

    public void unhighlightAllSlots()
    {
        searchHotbarWidgets.forEach(widget -> widget.setHighlighted(false));
    }

    public void onHotbarKeyPressed(SearchHotbarWidget widget, int modifiers)
    {
        // if shift pressed, select hotbar slot only
        if (isModifierPressed(modifiers, GLFW.GLFW_MOD_SHIFT))
        {
            this.selectedHotbarWidget = widget;
            this.highlightSlot(selectedHotbarWidget.getHotbarIndex());
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
        {
            // if there is item in the selected hotbar slot
            SearchResultData item = widget.getSearchResultData();

            // default air, so if no item selected, it will clear the slot
            String identifier = "minecraft:air";
            int count = 1;
            // if a slot is already selected, use that slot
            if (selectedHotbarWidget != null)
            {
                if (selectedHotbarWidget.getSearchResultData() != null)
                {
                    identifier = selectedHotbarWidget.getSearchResultData()
                                                     .getIdentifier()
                                                     .toString();
                    count = selectedHotbarWidget.getSearchResultData().getIcon().getMaxStackSize();
                }

                // used this one
                selectedHotbarWidget = null;
                this.unhighlightAllSlots();
            }
            else if (item != null)
            {
                identifier = item.getIdentifier().toString();
                count = item.getMaxStackSize();
            }

            // replace item in hotbar slot
            String command = String.format(
                    "item replace entity @s hotbar.%d with %s %d",
                    widget.getHotbarIndex(),
                    identifier,
                    count
            );

            player.connection.sendCommand(command);
        }
    }

    /* STATICS */

    private static boolean isModifierPressed(int modifiers, int modifierToCheck)
    {
        return (modifiers & modifierToCheck) != 0;
    }

    /* GETTERS */

    public ArrayList<SearchHotbarWidget> getWidgets()
    {
        return searchHotbarWidgets;
    }

    /* FOCUS */

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        if (onFocusChanged != null)
        {
            onFocusChanged.accept(focused);
        }
        searchHotbarWidgets.forEach(w -> w.setShowBind(focused));
    }

    public void setOnFocusChanged(Consumer<Boolean> onFocusChanged)
    {
        this.onFocusChanged = onFocusChanged;
    }

    /* PRIVATE HELPERS */

    private enum HotbarHelpText
    {
        UNSELECTED("[HOTBAR KEY] to move item to that slot, Shift + [HOTBAR KEY] to select slot"),
        SELECTED("[HOTBAR KEY] to move item to that slot, Esc to unselect");

        private final String text;

        HotbarHelpText(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }
    }

    /* OVERRIDES */

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
}