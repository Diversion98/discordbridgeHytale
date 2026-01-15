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
import com.marblehalledgaming.linking.LinkManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public class UnlinkCommand extends AbstractPlayerCommand {

    public UnlinkCommand() {
        super("unlink", "Remove your Discord account link", false);
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
        UUID playerUuid = playerRef.getUuid();

        if (LinkManager.removeLink(playerUuid)) {
            playerRef.sendMessage(Message.raw("Your Discord account has been unlinked.").color(Color.GREEN));
        } else {
            playerRef.sendMessage(Message.raw("You don't have a linked account!").color(Color.RED));
        }
    }
}