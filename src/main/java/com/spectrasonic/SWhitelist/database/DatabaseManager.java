package com.spectrasonic.SWhitelist.database;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DatabaseManager {

    private final Main plugin;
    private Connection connection;

    public DatabaseManager(Main plugin) throws SQLException {
        this.plugin = plugin;
        initializeDatabase();
    }

    // Inicializar base de datos y crear tablas
    private void initializeDatabase() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "swhitelist.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        
        // Crear tablas si no existen
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS whitelist (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
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
            
            // Insertar configuración por defecto si no existe
            stmt.execute("""
                INSERT OR IGNORE INTO settings (key, value) 
                VALUES ('whitelist_enabled', 'false')
            """);
        }
    }

    // Verificar si la whitelist está habilitada
    public boolean isWhitelistEnabled() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT value FROM settings WHERE key = 'whitelist_enabled'")) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getString("value").equalsIgnoreCase("true");
        }
    }

    // Habilitar whitelist
    public void enableWhitelist() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE settings SET value = 'true' WHERE key = 'whitelist_enabled'")) {
            stmt.executeUpdate();
        }
    }

    // Deshabilitar whitelist
    public void disableWhitelist() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE settings SET value = 'false' WHERE key = 'whitelist_enabled'")) {
            stmt.executeUpdate();
        }
    }

    // Verificar si un jugador existe en la whitelist
    public boolean doesPlayerExist(String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) AS count FROM whitelist WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        }
    }

    // Agregar jugador a la whitelist
    public void addPlayer(String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO whitelist (username) VALUES (?)")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    // Remover jugador de la whitelist
    public void removePlayer(String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM whitelist WHERE username = ?")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    // Verificar si un jugador está en la whitelist
    public boolean isWhitelisted(String username) throws SQLException {
        return doesPlayerExist(username);
    }

    // Obtener todos los jugadores en la whitelist
    public List<String> getAllPlayers() throws SQLException {
        List<String> players = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM whitelist ORDER BY username")) {
            while (rs.next()) {
                players.add(rs.getString("username"));
            }
        }
        return players;
    }

    // Cerrar conexión
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error al cerrar la conexión a la base de datos: " + e.getMessage());
            }
        }
    }
}
