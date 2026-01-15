package com.marblehalledgaming.linking;

import java.util.HashMap;
import java.util.Map;

public class AvatarCache {
    private static final long EXPIRE_TIME = 1800000; // 30 Minutes
    private static final Map<String, Entry> cache = new HashMap<>();

    private record Entry(String url, long timestamp) {}

    public static synchronized String get(String discordId) {
        Entry e = cache.get(discordId);
        if (e != null) {
            if (System.currentTimeMillis() - e.timestamp < EXPIRE_TIME) {
                return e.url;
            } else {
                cache.remove(discordId); // Remove expired
            }
        }
        return null;
    }

    public static synchronized void put(String discordId, String url) {
        if (discordId == null || url == null) return;
        cache.put(discordId, new Entry(url, System.currentTimeMillis()));
    }
}