package com.marblehalledgaming;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.ArrayList;
import java.util.List;

public class DiscordBridgeConfig {

    // General Settings
    public String BotToken = "your_token_here";
    public String ChannelId = "0";
    public String BotName = "Hytale Bridge Bot";
    public String BotAvatarUrl = "";
    public boolean UseWebhooks = false;
    public String WebhookUrl = "";
    public List<String> AdminIds = new ArrayList<>();
    public List<String> PublicCommands = new ArrayList<>(List.of("list:list", "tps:tps"));

    // Database & Linking
    public boolean EnableLinking = false;
    public boolean UseSql = false;
    public String SqlHost = "localhost";
    public int SqlPort = 3306;
    public String SqlUser = "root";
    public String SqlPass = "password";
    public String SqlDb = "discord_bridge";

    // Event Messages
    public String ServerStartMessage = "✅ **Server has started!**";
    public String ServerStopMessage = "🛑 **Server is shutting down...**";
    public String JoinMessage = "📥 %player% joined the server.";
    public String LeaveMessage = "📤 %player% left the server.";
    public String DeathMessageFormat = "☠️ **%player%**";

    public DiscordBridgeConfig() {
    }

    public static final BuilderCodec<DiscordBridgeConfig> CODEC = BuilderCodec.builder(DiscordBridgeConfig.class, DiscordBridgeConfig::new)
            // String mapping
            .append(new KeyedCodec<String>("Bot_token", Codec.STRING),
                    (c, v, i) -> c.BotToken = v,
                    (c, i) -> c.BotToken)
            .add()

            .append(new KeyedCodec<String>("Channel ID", Codec.STRING),
                    (c, v, i) -> c.ChannelId = v,
                    (c, i) -> c.ChannelId)
            .add()

            .append(new KeyedCodec<String>("Bot Name", Codec.STRING),
                    (c, v, i) -> c.BotName = v,
                    (c, i) -> c.BotName)
            .add()

            .append(new KeyedCodec<String>("Avatar URL", Codec.STRING),
                    (c, v, i) -> c.BotAvatarUrl = v,
                    (c, i) -> c.BotAvatarUrl)
            .add()

            .append(new KeyedCodec<Boolean>("Use Webhooks", Codec.BOOLEAN),
                    (c, v, i) -> c.UseWebhooks = v,
                    (c, i) -> c.UseWebhooks)
            .add()

            .append(new KeyedCodec<String>("Webhook URL", Codec.STRING),
                    (c, v, i) -> c.WebhookUrl = v,
                    (c, i) -> c.WebhookUrl)
            .add()

            .append(new KeyedCodec<Boolean>("Enable Linking", Codec.BOOLEAN),
                    (c, v, i) -> c.EnableLinking = v,
                    (c, i) -> c.EnableLinking)
            .add()

            .append(new KeyedCodec<Boolean>("Use SQL for linking", Codec.BOOLEAN),
                    (c, v, i) -> c.UseSql = v,
                    (c, i) -> c.UseSql)
            .add()

            .append(new KeyedCodec<String>("SQL Host", Codec.STRING),
                    (c, v, i) -> c.SqlHost = v,
                    (c, i) -> c.SqlHost)
            .add()

            .append(new KeyedCodec<Integer>("SQL Port", Codec.INTEGER),
                    (c, v, i) -> c.SqlPort = v,
                    (c, i) -> c.SqlPort)
            .add()

            .append(new KeyedCodec<String>("SQL User", Codec.STRING),
                    (c, v, i) -> c.SqlUser = v,
                    (c, i) -> c.SqlUser)
            .add()

            .append(new KeyedCodec<String>("SQL Password", Codec.STRING),
                    (c, v, i) -> c.SqlPass = v,
                    (c, i) -> c.SqlPass)
            .add()

            .append(new KeyedCodec<String>("SQL Database", Codec.STRING),
                    (c, v, i) -> c.SqlDb = v,
                    (c, i) -> c.SqlDb)
            .add()

            // Message mapping
            .append(new KeyedCodec<>("Server Start Message", Codec.STRING),
                    (c, v, i) -> c.ServerStartMessage = v,
                    (c, i) -> c.ServerStartMessage)
            .add()

            .append(new KeyedCodec<>("Join Message (Use %player% to user the playername)", Codec.STRING),
                    (c, v, i) -> c.JoinMessage = v,
                    (c, i) -> c.JoinMessage)
            .add()
            .append(new KeyedCodec<>("Leave Message (Use %player% to user the playername)", Codec.STRING),
                    (c, v, i) -> c.LeaveMessage = v,
                    (c, i) -> c.LeaveMessage)
            .add()

            .append(new KeyedCodec<>("Server Stop Message", Codec.STRING),
                    (c, v, i) -> c.ServerStopMessage = v,
                    (c, i) -> c.ServerStopMessage)
            .add()
            .build();
    }