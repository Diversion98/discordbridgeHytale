package com.marblehalledgaming.events;

import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.marblehalledgaming.DiscordBridgeConfig;
import com.marblehalledgaming.DiscordBridgeHT;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Events {
    private static final Map<UUID, Long> disconnectCooldown = new HashMap<>();

    private static DiscordBridgeConfig getConfig() {
        return DiscordBridgeHT.getInstance().getBridgeConfig();
    }

    public static void onPlayerConnect(PlayerConnectEvent event) {
        String username = event.getPlayerRef().getUsername();
        DiscordBridgeConfig config = getConfig();

        // 2. Dispatch to Discord
        String content = config.JoinMessage.replace("%player%", username);
        DiscordBridgeHT.dispatchToDiscord(null, config.BotName, content, config.BotAvatarUrl);
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if (DiscordBridgeHT.getInstance().isShuttingDown()) {
            return;
        }

        UUID uuid = event.getPlayerRef().getUuid();
        long now = System.currentTimeMillis();

        // If they "disconnected" less than 1 seconds ago, ignore the duplicate
        if (disconnectCooldown.containsKey(uuid) && (now - disconnectCooldown.get(uuid) < 1000)) {
            return;
        }

        disconnectCooldown.put(uuid, now);

        DiscordBridgeConfig config = getConfig();

        String username = event.getPlayerRef().getUsername();
        String content = config.LeaveMessage.replace("%player%", username);

        DiscordBridgeHT.dispatchToDiscord(null, config.BotName, content, config.BotAvatarUrl);
    }

    public static void onStartup(BootEvent event) {
        DiscordBridgeConfig config = getConfig();
        DiscordBridgeHT.dispatchToDiscord(null, config.BotName, config.ServerStartMessage, config.BotAvatarUrl);
    }

    public static void onShutdown(ShutdownEvent event) {
        DiscordBridgeConfig config = getConfig();
        DiscordBridgeHT.dispatchToDiscord(null, config.BotName, config.ServerStopMessage, config.BotAvatarUrl);
    }

    public static void onMessage(PlayerChatEvent event) {
        // Filter out commands or internal system messages
        if (event.getContent().startsWith("/") || event.getContent().isEmpty()) {
            return;
        }

        PlayerRef player = event.getSender();

        DiscordBridgeHT.dispatchToDiscord(
                player.getUuid(),
                player.getUsername(),
                event.getContent(),
                getConfig().BotAvatarUrl
        );
    }
}