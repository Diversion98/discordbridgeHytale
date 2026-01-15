package com.marblehalledgaming;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.marblehalledgaming.commands.LinkCommand;
import com.marblehalledgaming.linking.LinkManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;

public class DiscordToHTListener extends ListenerAdapter {

    private final DiscordBridgeHT plugin;

    public DiscordToHTListener(DiscordBridgeHT plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the new /link slash command
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("link")) {
            String code = Objects.requireNonNull(event.getOption("code")).getAsString();
            UUID playerUuid = LinkCommand.PENDING_CODES.get(code);

            if (playerUuid != null) {
                String today = java.time.LocalDate.now().toString();
                LinkManager.addLink(playerUuid, event.getUser().getId(), today);

                LinkCommand.PENDING_CODES.remove(code);

                event.reply("✅ Successfully linked to your Hytale account!")
                        .setEphemeral(true)
                        .queue();

                PlayerRef player = Universe.get().getPlayer(playerUuid);
                if (player != null) {
                    player.sendMessage(Message.raw("Your account has been linked to Discord!").color(Color.GREEN));
                }
            } else {
                event.reply("❌ Invalid or expired code. Type `/db link` in-game to get a new one.")
                        .setEphemeral(true)
                        .queue();
            }
        }

        if (event.getName().equals("checklink")) {
            String discordId = event.getUser().getId();
            UUID hytaleUuid = LinkManager.getUuidFromDiscordId(discordId);

            if (hytaleUuid == null) {
                event.reply("❌ Your Discord account is not linked to a Hytale account.")
                        .setEphemeral(true).queue();
                return;
            }

            String date = LinkManager.getLinkDate(hytaleUuid);

            event.reply("✅ **Link Found!**\n" +
                            "**Hytale UUID:** `" + hytaleUuid + "`\n" +
                            "**Linked On:** " + date)
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Handles standard chat bridging (unchanged, but removed the old !link check)
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        DiscordBridgeConfig config = plugin.getBridgeConfig();
        if (!event.getChannel().getId().equals(config.ChannelId)) return;

        Message discordMessage = Message.join(
                Message.raw("[Discord] ").color(Color.decode("#7289DA")),
                Message.raw(event.getAuthor().getEffectiveName()).color(Color.WHITE),
                Message.raw(": " + event.getMessage().getContentDisplay()).color(Color.WHITE)
        );

        Universe.get().sendMessage(discordMessage);
    }
}