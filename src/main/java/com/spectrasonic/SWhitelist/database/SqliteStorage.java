package com.spectrasonic.SWhitelist.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend SQLite (jdbc:sqlite). El driver lo incluye Paper.
 */
public class SqliteStorage implements WhitelistStorage {

    private final Connection connection;

    public SqliteStorage(File dbFile) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS whitelist (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE COLLATE BINARY,
                            discord_id TEXT,
                            added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS settings (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            key TEXT NOT NULL UNIQUE,
                            value TEXT NOT NULL
                        )
                    """);

            stmt.execute("""
                        INSERT OR IGNORE INTO settings (key, value)
                        VALUES ('whitelist_enabled', 'false')
                    """);

            // Migración: agregar discord_id si no existe
            try {
                stmt.execute("ALTER TABLE whitelist ADD COLUMN discord_id TEXT");
            } catch (SQLException ignored) {
                // La columna ya existe
            }
        }
    }

    @Override
    public List<String> loadPlayers() {
        List<String> players = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT username FROM whitelist")) {
            while (rs.next()) {
                players.add(rs.getString("username"));
            }
        } catch (SQLException ignored) {
            // El caché vuelve a intentarlo en la próxima recarga
        }
        return players;
    }

    @Override
    public boolean loadWhitelistEnabled() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT value FROM settings WHERE key = 'whitelist_enabled'")) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getString("value").equalsIgnoreCase("true");
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void saveWhitelistEnabled(boolean enabled) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE settings SET value = ? WHERE key = 'whitelist_enabled'")) {
            stmt.setString(1, enabled ? "true" : "false");
            stmt.executeUpdate();
        }
    }

    @Override
    public void addPlayer(String username, String discordId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO whitelist (username, discord_id) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, discordId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removePlayer(String username) throws SQLException {
        // Coincidencia case-insensitive: se normaliza a minúsculas en ambos lados
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM whitelist WHERE username COLLATE NOCASE = ?")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean doesPlayerExist(String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) AS count FROM whitelist WHERE username COLLATE NOCASE = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        }
    }

    @Override
    public String getDiscordId(String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT discord_id FROM whitelist WHERE username COLLATE NOCASE = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("discord_id");
            }
            return null;
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
