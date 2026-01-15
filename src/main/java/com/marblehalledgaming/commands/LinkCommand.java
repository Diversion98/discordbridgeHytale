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
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkCommand extends AbstractPlayerCommand {
    public static final Map<String, UUID> PENDING_CODES = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public LinkCommand() {
        super("link", "Link your Discord account");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
        UUID playerUuid = playerRef.getUuid();
        Message commandMessage = Message.raw("Use command /link in the " + DiscordBridgeHT.getInstance().getLinkedGuildName() + " Discord channel '" + DiscordBridgeHT.getInstance().getLinkedChannelName() + "' with your code.").color(Color.cyan);

        // 1. Check if the player is already permanently linked in the database
        if (LinkManager.isLinked(playerUuid)) {
            playerRef.sendMessage(
                    Message.raw("Your account is already linked to Discord!").color(Color.RED)
            );
            return;
        }

        // 2. Check if the player already has an active code
        String existingCode = PENDING_CODES.entrySet().stream()
                .filter(entry -> entry.getValue().equals(playerUuid))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (existingCode != null) {
            playerRef.sendMessage(Message.raw("--------------------------------").color(Color.GRAY));
            playerRef.sendMessage(Message.join(
                    Message.raw("You already have an active link code: ").color(Color.GREEN),
                    Message.raw(existingCode).color(Color.YELLOW).bold(true)
            ));
            playerRef.sendMessage(commandMessage);
            playerRef.sendMessage(Message.raw("--------------------------------").color(Color.GRAY));
            return;
        }

        String code = String.format("%04d", random.nextInt(10000));
        PENDING_CODES.put(code, playerUuid);

        playerRef.sendMessage(Message.raw("--------------------------------").color(Color.GRAY));
        playerRef.sendMessage(Message.join(
                Message.raw("Your Discord Link Code is: "),
                Message.raw(code).color(Color.YELLOW).bold(true)
        ));

        playerRef.sendMessage(commandMessage);
        playerRef.sendMessage(Message.raw("--------------------------------").color(Color.GRAY));

        // Expire code after 5 minutes
        java.util.concurrent.CompletableFuture.delayedExecutor(5, java.util.concurrent.TimeUnit.MINUTES)
                .execute(() -> PENDING_CODES.remove(code));
    }
}