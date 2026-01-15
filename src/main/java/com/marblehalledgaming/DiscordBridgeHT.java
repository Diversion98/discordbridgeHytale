package com.marblehalledgaming;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.*;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.marblehalledgaming.commands.DiscordBridgeCommand;
import com.marblehalledgaming.events.Events;
import com.marblehalledgaming.linking.AvatarCache;
import com.marblehalledgaming.linking.LinkManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DiscordBridgeHT extends JavaPlugin {

    private static DiscordBridgeHT instance;
    private final Config<DiscordBridgeConfig> configHandle;
    private static JDA jda;
    private boolean isShuttingDown = false;

    private static final java.net.http.HttpClient HTTP_CLIENT = java.net.http.HttpClient.newHttpClient();
    public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBridgeHT.class);

    public DiscordBridgeHT(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        // In 2026, the CODEC handles the JSON mapping automatically
        this.configHandle = this.withConfig("config", DiscordBridgeConfig.CODEC);
    }

    public static DiscordBridgeHT getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER.info("[DiscordBridge] Starting setup...");

        var registry = this.getEventRegistry();
        registry.registerGlobal(PlayerConnectEvent.class, Events::onPlayerConnect);
        registry.registerGlobal(PlayerDisconnectEvent.class, Events::onPlayerDisconnect);
        registry.registerGlobal(BootEvent.class, Events::onStartup);
        registry.registerGlobal(ShutdownEvent.class, Events::onShutdown);
        registry.registerGlobal(PlayerChatEvent.class, Events::onMessage);

        this.configHandle.save();

        if (getBridgeConfig().EnableLinking) {
            LinkManager.init(this.getDataDirectory(), getBridgeConfig());
            this.getCommandRegistry().registerCommand(new DiscordBridgeCommand());
        }

        DiscordBridgeConfig config = getBridgeConfig();
        if (isTokenValid(config.BotToken)) {
            initJDA(config.BotToken);
        } else {
            LOGGER.warn("[DiscordBridge] No valid BotToken found. Discord features will be disabled until configured.");
        }
    }

    @Override
    protected void shutdown() {
        this.isShuttingDown = true;
        LOGGER.info("[DiscordBridge] Cleaning up JDA resources...");
        if (jda != null) {
            try {
                // Inform Discord we are leaving and wait up to 5 seconds for threads to close
                jda.shutdown();
                if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }
                LOGGER.info("[DiscordBridge] JDA shutdown successfully.");
            } catch (InterruptedException e) {
                jda.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isShuttingDown() {
        return isShuttingDown;
    }

    public String getLinkedChannelName() {
        if (jda == null) return "Unknown (Bot Offline)";

        // 1. Retrieve the TextChannel object using the ID from your config
        var channel = jda.getTextChannelById(getBridgeConfig().ChannelId);

        // 2. Return the name if the channel exists, otherwise return a fallback
        return (channel != null) ? channel.getName() : "Unknown (Invalid ID)";
    }

    public String getLinkedGuildName() {
        if (jda == null) return "Unknown (Offline)";

        var channel = jda.getTextChannelById(getBridgeConfig().ChannelId);

        // If channel exists, get its guild and then the guild's name
        return (channel != null) ? channel.getGuild().getName() : "Unknown Guild";
    }

    private void initJDA(String token) {
        try {
            jda = JDABuilder.createLight(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new DiscordToHTListener(this))
                    .build();

            jda.upsertCommand("link", "Link your Discord account to Hytale")
                    .addOption(OptionType.STRING, "code", "The 4-digit code from in-game", true)
                    .queue();

            jda.upsertCommand("checklink", "Check which Hytale account is linked to your Discord")
                    .queue();

            jda.awaitReady();
            LOGGER.info("[DiscordBridge] JDA Connected to Discord as: {}", jda.getSelfUser().getAsTag());

            updateBotProfile();
        } catch (Exception e) {
            LOGGER.error("[DiscordBridge] JDA failed to login. Check your token or internet connection.");
            jda = null;
        }
    }

    public static JDA getJda() {
        return jda;
    }

    public DiscordBridgeConfig getBridgeConfig() {
        return this.configHandle.get();
    }

    private boolean isTokenValid(String token) {
        return token != null && !token.isEmpty() && !token.equalsIgnoreCase("PUT TOKEN HERE");
    }

    public static void sendMessageToDiscord(String content) {
        if (jda != null) {
            DiscordBridgeConfig config = instance.getBridgeConfig();
            var channel = jda.getTextChannelById(config.ChannelId);
            if (channel != null) {
                channel.sendMessage(content).queue();
            }
        }
    }


    public static void sendWebhookMessage(String username, String content, String avatarUrl) {
        DiscordBridgeConfig config = instance.getBridgeConfig();

        if (!config.UseWebhooks || config.WebhookUrl == null || config.WebhookUrl.isEmpty()) {
            sendMessageToDiscord("**" + username + "**: " + content);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("content", content);
        if (avatarUrl != null) json.addProperty("avatar_url", avatarUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.WebhookUrl))
                .header("Content-Type", "application/json")
                // ADD THIS LINE: Discord requires a User-Agent for bot requests
                .header("User-Agent", "HytaleDiscordBridge/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    // If it still fails, this will tell us exactly why in the console
                    if (response.statusCode() >= 400) {
                        LOGGER.error("[DiscordBridge] Webhook Error: {} - {}", response.statusCode(), response.body());
                    }
                })
                .exceptionally(ex -> {
                    LOGGER.error("[DiscordBridge] Webhook Network Error: {}", ex.getMessage());
                    return null;
                });
    }

    public static void dispatchToDiscord(UUID playerUuid, String name, String content, String fallbackAvatar) {
        DiscordBridgeConfig config = instance.getBridgeConfig();
        if (config == null) return;

        // IF WEBHOOKS ARE OFF: Use standard bot message and EXIT
        if (!config.UseWebhooks) {
            if (!Objects.equals(name, config.BotName)) {
                sendMessageToDiscord("**" + name + "**: " + content);
            } else {
                sendMessageToDiscord(content);
            }
            return;
        }

        // IF WEBHOOKS ARE ON:
        try {
            String discordId = (playerUuid != null) ? LinkManager.getDiscordId(playerUuid) : null;

            // If no link, send immediately and stop
            if (discordId == null || jda == null) {
                sendWebhookMessage(name, content, fallbackAvatar);
                return;
            }

            // Try Cache
            String cached = AvatarCache.get(discordId);
            if (cached != null) {
                sendWebhookMessage(name, content, cached);
                return;
            }

            // If not in cache, fetch ASYNC.
            // We do NOT 'return' inside the queue to avoid logic gaps.
            jda.retrieveUserById(discordId).queue(
                    user -> {
                        String avatar = user.getEffectiveAvatarUrl();
                        AvatarCache.put(discordId, avatar);
                        sendWebhookMessage(name, content, avatar);
                    },
                    throwable -> sendWebhookMessage(name, content, fallbackAvatar)
            );

        } catch (Exception e) {
            // Absolute emergency fallback: if the code above crashes, send the message anyway
            LOGGER.error("[DiscordBridge] Dispatch error: {}", e.getMessage());
            sendWebhookMessage(name, content, fallbackAvatar);
        }
    }

    private void updateBotProfile() {
        if (jda == null) return;
        DiscordBridgeConfig config = getBridgeConfig();

        // 1. Get the manager
        var manager = jda.getSelfUser().getManager();

        // 2. Queue the name change locally in the manager
        if (config.BotName != null && !config.BotName.equals("Bridge Bot")) {
            manager.setName(config.BotName);
        }

        // 3. Queue the avatar change locally in the manager
        if (config.BotAvatarUrl != null && config.BotAvatarUrl.startsWith("http")) {
            try {
                java.net.URI uri = java.net.URI.create(config.BotAvatarUrl);
                try (var is = uri.toURL().openStream()) {
                    manager.setAvatar(net.dv8tion.jda.api.entities.Icon.from(is));
                }
            } catch (Exception e) {
                LOGGER.error("[DiscordBridge] Failed to load avatar: {}", e.getMessage());
            }
        }
    }
}