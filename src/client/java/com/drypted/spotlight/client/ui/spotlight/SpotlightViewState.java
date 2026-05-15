package com.drypted.spotlight.client.ui.spotlight;

import com.drypted.spotlight.client.core.blueprints.ItemsResultData;
import org.jetbrains.annotations.Nullable;

public final class SpotlightViewState
{
    private @Nullable ItemsResultData submitItemResult;

    public @Nullable ItemsResultData getSubmitItemResult()
    {
        return submitItemResult;
    }

    public void setSubmitItemResult(@Nullable ItemsResultData submitItemResult)
    {
        this.submitItemResult = submitItemResult;
    }
}
