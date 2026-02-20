package com.drypted.spotlight.client.core.commands.args;

import com.drypted.spotlight.client.SpotlightEntryClient;
import com.drypted.spotlight.client.core.actions.GiveItemAction;
import com.drypted.spotlight.client.core.commands.Command;
import com.drypted.spotlight.client.core.commands.CommandFeedback;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;

public class GetPlayerHeadCommand implements Command
{
    @Override
    public boolean requiresArgs()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "head";
    }

    @Override
    public String getDescription()
    {
        return "Get the head of a player by username";
    }

    @Override
    public CommandFeedback validateArgs(String[] args)
    {
        if (args.length != 1) return CommandFeedback.withError("Please enter a username");

        String username = args[0].trim();
        if (!username.matches("^[a-zA-Z0-9_]{3,16}$"))
            return CommandFeedback.withError("Please enter a valid username");

        return CommandFeedback.NO_ERROR;
    }

    @Override
    public CommandFeedback execute(String[] args, LocalPlayer player)
    {
        CommandFeedback argsError = validateArgs(args);
        if (argsError.isCritical()) return argsError;
        else if (!argsError.isNone()) player.displayClientMessage(
                Component.literal(argsError.getSeverity().getName() + ": " + argsError.getMessage()) //
                        .withStyle(ChatFormatting.GOLD), false
        );

        // fetch player head
        String playerName = args[0];

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);


        head.set(
                DataComponents.PROFILE,
                new ResolvableProfile(Optional.ofNullable(playerName), Optional.empty(), new PropertyMap())
        );
        head.setCount(1);


        SpotlightEntryClient.LOGGER.info(head.getComponents().toString());

        GiveItemAction.run(player, head, playerName + " Head");

        return CommandFeedback.NO_ERROR;
    }
}
