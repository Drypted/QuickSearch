package com.drypted.spotlight.client.ui.spotlight;

public final class SpotlightSubmitHandler
{
    private final SpotlightSubmitCommandHandler commandHandler;
    private final SpotlightSubmitItemHandler itemHandler;

    public SpotlightSubmitHandler(SpotlightSubmitCommandHandler commandHandler, SpotlightSubmitItemHandler itemHandler)
    {
        this.commandHandler = commandHandler;
        this.itemHandler = itemHandler;
    }

    public void submit(String text, boolean isCommandInput)
    {
        if (isCommandInput)
        {
            commandHandler.submit(text);
            return;
        }

        itemHandler.submit();
    }
}
