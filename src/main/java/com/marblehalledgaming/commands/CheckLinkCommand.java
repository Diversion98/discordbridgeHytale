package com.marblehalledgaming.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.marblehalledgaming.DiscordBridgeHT;
import com.marblehalledgaming.linking.LinkManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CheckLinkCommand extends AbstractPlayerCommand {
    public CheckLinkCommand() {
        super("checklink", "Check your linked Discord account info");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
        if (!LinkManager.isLinked(playerRef.getUuid())) {
            playerRef.sendMessage(Message.raw("You don't have a linked Discord account.").color(Color.RED));
            return;
        }

        String discordId = LinkManager.getDiscordId(playerRef.getUuid());
        String date = LinkManager.getLinkDate(playerRef.getUuid());

        DiscordBridgeHT.getJda().retrieveUserById(discordId).queue(user -> {
            String username = user.getName();

            playerRef.sendMessage(Message.raw("--------------------------------").color(Color.GRAY));
            playerRef.sendMessage(Message.raw("Discord Link Info:").color(Color.cyan));
            playerRef.sendMessage(Message.raw("Account: " + username));
            playerRef.sendMessage(Message.raw("Date Linked: " + date));
            playerRef.sendMessage(Message.raw("--------------------------------").color(Color.GRAY));
        }, throwable -> {
            // Fallback if the user left Discord or the ID is invalid
            playerRef.sendMessage(Message.raw("Account ID: " + discordId + " (User not found)"));
        });
    }

}
