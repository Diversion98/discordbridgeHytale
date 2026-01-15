DiscordBridgeHT

A high-performance Hytale to Discord bridge featuring asynchronous webhooks, account linking, and smart avatar caching.

🚀 Features

- Dynamic Webhooks: Messages appear in Discord with the player's name and their custom avatar.
- Account Linking: /link system to pair Hytale UUIDs with Discord Accounts.
- Smart Caching: Local AvatarCache stores Discord profile pictures for 30 minutes to stay within rate limits.
- Async Execution: Zero lag on your Hytale server threads—all networking happens in the background.
- Dual Mode: Use advanced Webhooks or standard Bot messages.

🛠️ Installation

1. Download the latest .jar from the Releases page.
2. Place the file in your Hytale server's mods directory.
3. Restart the server to generate config/DiscordBridgeHT/config.json.
4. Configure your Bot Token, Channel ID, and Webhook URL (see Configuration below).
  
⚙️ Configuration

Your config.json handles the core logic of the bridge.

Key              Description
BotToken         Your Discord Bot Token from the Developer Portal.
ChannelId        The ID of the text channel for the bridge.
UseWebhooks      Set to true to use player avatars and names.
WebhookUrl       The Webhook URL from your Discord channel settings.
EnableLinking    Toggle the /link and /checklink system.

🔐 Privileged Member Intents

To fetch player avatars and read message content, you must enable these settings in the Discord Developer Portal:
1. Navigate to the Bot tab.
2. Enable Presence Intent.
3. Enable Server Members Intent.
4. Enable Message Content Intent.

💾 Storage:

JSON vs SQL
By default, the plugin uses JSON for easy setup, but it supports SQLite/MySQL for larger networks.

JSON (Default)File: LinkData.json
Pros: No setup required, human-readable.
Cons: Slows down slightly with thousands of registered players and every server needs its own JSON file.

SQL (Recommended for 100+ players)
To switch to SQL, change the StorageType in your config to SQL.
Pros: Instant lookups, concurrent access, database backups and multiple servers can rely on this.
Setup: The plugin will automatically create the linked_players table on the first run.

🎮 Commands & Permissions

Command              Usage
/link <code>         Link your Discord account.
/unlink              Unlink your Discord account.
/checklink           View your current link status.
