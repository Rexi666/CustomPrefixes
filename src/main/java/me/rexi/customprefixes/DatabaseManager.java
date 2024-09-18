package me.rexi.customprefixes;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager(FileConfiguration config, String dbType) {
        File dataDir = new File("plugins/CustomPrefixes/data");
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                throw new RuntimeException("Failed to create data directory.");
            }
        }

        try {
            if (dbType.equalsIgnoreCase("sqlite")) {
                String dbPath = "plugins/CustomPrefixes/data/database.db";
                SQLiteDataSource dataSource = new SQLiteDataSource();
                dataSource.setUrl("jdbc:sqlite:" + dbPath);
                connection = dataSource.getConnection();
            } else if (dbType.equalsIgnoreCase("mysql")) {
                String host = config.getString("database.mysql.host");
                int port = config.getInt("database.mysql.port");
                String database = config.getString("database.mysql.database");
                String user = config.getString("database.mysql.username");
                String password = config.getString("database.mysql.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                connection = DriverManager.getConnection(url, user, password);
            } else {
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
            }
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize the database connection", e);
        }
    }

    private void createTables() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS player_tags (" +
                        "player_id VARCHAR(36) PRIMARY KEY, " +
                        "tag TEXT, " +
                        "last_changed BIGINT)")) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getLastTagChange(UUID playerId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT last_changed FROM player_tags WHERE player_id = ?")) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("last_changed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if no record found
    }

    public void updatePlayerTag(UUID playerId, String tag, long timestamp) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "REPLACE INTO player_tags (player_id, tag, last_changed) VALUES (?, ?, ?)")) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, tag);
            stmt.setLong(3, timestamp);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerTag(UUID playerId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT tag FROM player_tags WHERE player_id = ?")) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no tag found
    }

    public void removeTag(UUID playerId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM player_tags WHERE player_id = ?")) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetCooldown(UUID playerId, long timestamp) {
        String sql = "UPDATE player_tags SET last_changed = 0 WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            int rowsAffected = stmt.executeUpdate();
            Bukkit.getLogger().info("Cooldown reset for player UUID: " + playerId + ". Rows affected: " + rowsAffected);

            if (rowsAffected == 0) {
                Bukkit.getLogger().warning("No rows updated for player UUID: " + playerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}