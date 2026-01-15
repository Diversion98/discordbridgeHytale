package com.marblehalledgaming.linking;

import com.marblehalledgaming.DiscordBridgeConfig;
import com.marblehalledgaming.DiscordBridgeHT;
import java.nio.file.Path;
import java.util.UUID;

public class LinkManager {
    private static LinkProvider provider;

    public static void init(Path dataDir, DiscordBridgeConfig config) {
        if (config.UseSql) {
            DiscordBridgeHT.LOGGER.info("[DiscordBridge] Using SQL for account linking.");
            provider = new SqlLinkProvider(config);
        } else {
            DiscordBridgeHT.LOGGER.info("[DiscordBridge] Using JSON for account linking.");
            provider = new JsonLinkProvider(dataDir.resolve("discord_links.json"));
        }
    }

    public static void addLink(UUID uuid, String discordId, String date) {
        provider.addLink(uuid, discordId, date);
    }

    public static UUID getUuidFromDiscordId(String discordId) {
        return provider.getUuidByDiscordId(discordId);
    }

    public static String getDiscordId(UUID uuid) { return provider.getDiscordId(uuid); }

    public static String getLinkDate(UUID uuid) { return provider.getLinkDate(uuid); }

    public static boolean isLinked(UUID uuid) { return provider.isLinked(uuid); }

    public static boolean removeLink(UUID uuid) { return provider.removeLink(uuid); }
}