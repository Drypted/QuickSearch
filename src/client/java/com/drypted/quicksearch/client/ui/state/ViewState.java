package com.drypted.quicksearch.client.ui.state;

import com.drypted.quicksearch.client.core.blueprints.ItemsResultData;
import org.jetbrains.annotations.Nullable;

public final class ViewState
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
