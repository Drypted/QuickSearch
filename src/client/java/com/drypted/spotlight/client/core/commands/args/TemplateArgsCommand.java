package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.core.blueprints.commands.ArgumentedCommand;
import com.drypted.spotlight.client.core.blueprints.commands.CommandFeedback;
import com.drypted.spotlight.client.core.blueprints.commands.argument.types.StringArgumentType;
import com.drypted.spotlight.client.core.blueprints.commands.argument.types.WordArgumentType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TemplateArgsCommand extends ArgumentedCommand
{
    public TemplateArgsCommand()
    {
        super(new WordArgumentType("<word>"), new StringArgumentType("<sentence>"));
    }

    @Override
    public String getName()
    {
        return "args";
    }

    @Override
    public String getDescription()
    {
        return "Test With Args commands.";
    }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback error = validateArgs(args);

        if (error.haltsExecution())
        {
            return error;
        }

        player.displayClientMessage(
                Component.literal("Test With Args commands executed with args \"" //
                        + String.join(", ", args) + "\""), true
        );

        return CommandFeedback.NO_ERROR;
    }
}
