package com.drypted.quicksearch.client.core.submit;

public final class SubmitHandler
{
    private final CommandSubmitHandler commandHandler;
    private final ItemSubmitHandler itemHandler;

    public SubmitHandler(CommandSubmitHandler commandHandler, ItemSubmitHandler itemHandler)
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
