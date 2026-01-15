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
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DiscordBridgeCommand extends AbstractPlayerCommand {

    public DiscordBridgeCommand() {
        super("discordbridge", "Main command for Discord linking", false);
        this.addAliases("db");
        this.addSubCommand(new LinkCommand());
        this.addSubCommand(new UnlinkCommand());
        this.addSubCommand(new CheckLinkCommand());

        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
        playerRef.sendMessage(Message.raw("Usage: /db <link|unlink|checklink>").color(Color.RED));
    }
}