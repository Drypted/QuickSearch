package com.drypted.quicksearch.client.ui.state;

import com.drypted.quicksearch.client.core.blueprints.ui.common.RoundedCorners;
import com.drypted.quicksearch.client.ui.components.HotbarCollectionWidget;
import com.drypted.quicksearch.client.ui.components.InputWidget;
import com.drypted.quicksearch.client.ui.components.ScrollBoxWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public final class VisibilityController
{
    private final InputWidget inputWidget;
    private final ScrollBoxWidget searchResultsWidget;
    private final @Nullable HotbarCollectionWidget hotbarCollectionWidget;
    private final BooleanSupplier isHotbarEnabled;

    public VisibilityController(InputWidget inputWidget, ScrollBoxWidget searchResultsWidget, @Nullable HotbarCollectionWidget hotbarCollectionWidget, BooleanSupplier isHotbarEnabled)
    {
        this.inputWidget = inputWidget;
        this.searchResultsWidget = searchResultsWidget;
        this.hotbarCollectionWidget = hotbarCollectionWidget;
        this.isHotbarEnabled = isHotbarEnabled;
    }

    public void setItemResultsVisible(boolean visible)
    {
        setHotbarWidgetVisible(visible);
        setVisible(this.searchResultsWidget, visible);

        // if (visible) this.inputWidget.setRounded(RoundedCorners.fromVerticalSides(true, false));
        // else this.inputWidget.setRounded(RoundedCorners.all());
    }

    public void setHotbarWidgetVisible(boolean visible)
    {
        if (!isHotbarEnabled.getAsBoolean() || this.hotbarCollectionWidget == null) return;

        this.hotbarCollectionWidget.getWidgets().forEach(widget -> setVisible(widget, visible));
        setVisible(this.hotbarCollectionWidget, visible);
    }

    public void clearHotbarResults()
    {
        if (!isHotbarEnabled.getAsBoolean() || this.hotbarCollectionWidget == null) return;

        this.hotbarCollectionWidget.getWidgets().forEach(widget -> widget.setSearchResultData(null));
    }

    public @Nullable HotbarCollectionWidget getHotbarCollectionWidget() { return hotbarCollectionWidget; }

    private static void setVisible(@Nullable AbstractWidget widget, boolean visible)
    {
        if (widget == null) return;

        widget.visible = visible;
        widget.active = visible;
    }
}
