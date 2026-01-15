package com.marblehalledgaming.linking;

import com.marblehalledgaming.DiscordBridgeConfig;
import com.marblehalledgaming.DiscordBridgeHT;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.UUID;

public class SqlLinkProvider implements LinkProvider {
    private final HikariDataSource dataSource;

    public SqlLinkProvider(DiscordBridgeConfig cfg) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        String jdbcUrl = "jdbc:mysql://" + cfg.SqlHost + ":" + cfg.SqlPort + "/" + cfg.SqlDb +
                "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(cfg.SqlUser);
        config.setPassword(cfg.SqlPass);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);

        this.dataSource = new HikariDataSource(config);
        setupTable();
    }

    private void setupTable() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Ensure table exists with VARCHAR date column
            stmt.execute("CREATE TABLE IF NOT EXISTS hytale_discord_links (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "discord_id VARCHAR(32) NOT NULL, " +
                    "link_date VARCHAR(10) NOT NULL)");
        } catch (SQLException e) {
            DiscordBridgeHT.LOGGER.error("[DiscordBridge] SQL Setup Error: {}", e.getMessage());
        }
    }

    @Override
    public void addLink(UUID uuid, String discordId, String date) {
        String query = "REPLACE INTO hytale_discord_links (uuid, discord_id, link_date) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, discordId);
            ps.setString(3, date); // Storing date as String
            ps.executeUpdate();
        } catch (SQLException e) {
            DiscordBridgeHT.LOGGER.error("[DiscordBridge] SQL Add Error: {}", e.getMessage());
        }
    }

    @Override
    public boolean isLinked(UUID uuid) {
        String query = "SELECT 1 FROM hytale_discord_links WHERE uuid = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeLink(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM hytale_discord_links WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public String getDiscordId(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM hytale_discord_links WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("discord_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UUID getUuidByDiscordId(String discordId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM hytale_discord_links WHERE discord_id = ?")) {
            ps.setString(1, discordId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return UUID.fromString(rs.getString("uuid"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public String getLinkDate(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT link_date FROM hytale_discord_links WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("link_date");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}