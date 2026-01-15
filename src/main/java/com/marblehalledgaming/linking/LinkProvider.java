package com.marblehalledgaming.linking;

import java.util.UUID;

public interface LinkProvider {
    void addLink(UUID uuid, String discordId, String date);
    String getDiscordId(UUID uuid);
    String getLinkDate(UUID uuid);
    boolean isLinked(UUID uuid);
    boolean removeLink(UUID uuid);
    UUID getUuidByDiscordId(String discordId);
}