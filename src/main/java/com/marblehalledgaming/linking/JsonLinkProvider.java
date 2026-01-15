package com.marblehalledgaming.linking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JsonLinkProvider implements LinkProvider {
    // Standard GSON with pretty printing, no custom adapters needed
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    // Outer Key: Player UUID | Inner Map: { "id": "discord_id", "date": "yyyy-mm-dd" }
    private Map<UUID, Map<String, String>> links = new ConcurrentHashMap<>();

    public JsonLinkProvider(Path filePath) {
        this.path = filePath;
        load();
    }

    @Override
    public void addLink(UUID uuid, String discordId, String date) {
        Map<String, String> data = new HashMap<>();
        data.put("id", discordId);
        data.put("date", date);

        links.put(uuid, data);
        save();
    }

    @Override
    public String getDiscordId(UUID uuid) {
        Map<String, String> data = links.get(uuid);
        return (data != null) ? data.get("id") : null;
    }

    @Override
    public String getLinkDate(UUID uuid) {
        Map<String, String> data = links.get(uuid);
        return (data != null) ? data.get("date") : "Unknown";
    }

    @Override
    public boolean isLinked(UUID uuid) {
        return links.containsKey(uuid);
    }

    @Override
    public boolean removeLink(UUID uuid) {
        if (links.remove(uuid) != null) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public UUID getUuidByDiscordId(String discordId) {
        for (Map.Entry<UUID, Map<String, String>> entry : links.entrySet()) {
            if (entry.getValue().get("id").equals(discordId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void load() {
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path)) {
            // Tells GSON to expect a Map where the values are also Maps
            TypeToken<Map<UUID, Map<String, String>>> type = new TypeToken<>() {};
            Map<UUID, Map<String, String>> loaded = GSON.fromJson(reader, type.getType());

            if (loaded != null) {
                this.links = new ConcurrentHashMap<>(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(links, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}