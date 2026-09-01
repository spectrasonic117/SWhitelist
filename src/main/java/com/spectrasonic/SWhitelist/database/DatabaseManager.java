package com.spectrasonic.SWhitelist.database;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@Getter
public class DatabaseManager {

    private final Main plugin;
    private final Connection connection;

    // ── In-memory caches (avoids per-login / per-player DB queries) ──
    private volatile boolean whitelistEnabled;
    private final Set<String> whitelistedPlayers = ConcurrentHashMap.newKeySet();

    // ── Single-thread executor for async DB writes ──
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SWhitelist-DB-Writer");
        t.setDaemon(true);
        return t;
    });

    public DatabaseManager(Main plugin) throws SQLException {
        this.plugin = plugin;
        this.connection = initializeDatabase();
        loadCache();
    }

    // ──────────────────────────── Database initialisation
    // ────────────────────────────

    private Connection initializeDatabase() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "database.db");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        try (Statement stmt = conn.createStatement()) {
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
        return conn;
    }

    // ──────────────────────────── Cache loading ────────────────────────────

    /**
     * Carga el estado completo de la whitelist en memoria.
     * Se llama al iniciar y al recargar.
     */
    private void loadCache() {
        try {
            // Cargar estado enabled/disabled
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT value FROM settings WHERE key = 'whitelist_enabled'")) {
                ResultSet rs = stmt.executeQuery();
                whitelistEnabled = rs.next() && rs.getString("value").equalsIgnoreCase("true");
            }

            // Cargar todos los nombres en el Set de caché respetando case-sensitive
            whitelistedPlayers.clear();
            boolean caseSensitive = getCollation().equals("BINARY");
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT username FROM whitelist")) {
                while (rs.next()) {
                    String name = rs.getString("username");
                    whitelistedPlayers.add(caseSensitive ? name : name.toLowerCase());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando caché de whitelist", e);
        }
    }

    /**
     * Recarga el caché completo desde la base de datos.
     * Llamar desde el comando /swhitelist reload.
     */
    public void reloadCache() {
        loadCache();
    }

    // ──────────────────────────── Cache-safe read methods (NO DB access)
    // ────────────────────────────

    /**
     * Verifica si la whitelist está habilitada — lee del caché, NO de la DB.
     */
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    // Obtener collation según config (BINARY = case-sensitive, NOCASE = insensible)
    private String getCollation() {
        try {
            if (plugin.getConfigManager() != null && !plugin.getConfigManager().isWhitelistCaseSensitive()) {
                return "NOCASE";
            }
        } catch (Exception ignored) {
        }
        return "BINARY";
    }

    // Verificar si un jugador existe en la whitelist
    public boolean doesPlayerExist(String username) throws SQLException {
        String collation = getCollation();
        String sql = "SELECT COUNT(*) AS count FROM whitelist WHERE username COLLATE " + collation + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        }
    }

    public void addPlayer(String username) throws SQLException {
        addPlayer(username, null);
    }

    public void addPlayer(String username, String discordId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO whitelist (username, discord_id) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, discordId);
            stmt.executeUpdate();
        }
        boolean caseSensitive = getCollation().equals("BINARY");
        whitelistedPlayers.add(caseSensitive ? username : username.toLowerCase());
    }

    public void removePlayer(String username) throws SQLException {
        String collation = getCollation();
        String sql = "DELETE FROM whitelist WHERE username COLLATE " + collation + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
        boolean caseSensitive = collation.equals("BINARY");
        whitelistedPlayers.remove(caseSensitive ? username : username.toLowerCase());
    }

    public void enableWhitelist() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE settings SET value = 'true' WHERE key = 'whitelist_enabled'")) {
            stmt.executeUpdate();
        }
        whitelistEnabled = true;
    }

    public void disableWhitelist() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE settings SET value = 'false' WHERE key = 'whitelist_enabled'")) {
            stmt.executeUpdate();
        }
        whitelistEnabled = false;
    }

    // ──────────────────────────── Async DB write helpers
    // ────────────────────────────

    /**
     * Ejecuta un update de whitelist_enabled de forma asíncrona.
     * Actualiza el caché en el hilo actual para que las lecturas sean inmediatas,
     * y programa la escritura a disco en background.
     */
    public void setWhitelistEnabledAsync(boolean enabled) {
        // Actualizar caché inmediatamente (thread-safe)
        whitelistEnabled = enabled;
        // Escritura async a la DB
        dbExecutor.submit(() -> {
            try {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE settings SET value = ? WHERE key = 'whitelist_enabled'")) {
                    stmt.setString(1, enabled ? "true" : "false");
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error actualizando whitelist_enabled en DB", e);
            }
        });
    }

    // ──────────────────────────── Data access (commands)
    // ────────────────────────────

    public String getDiscordId(String username) throws SQLException {
        String collation = getCollation();
        String sql = "SELECT discord_id FROM whitelist WHERE username COLLATE " + collation + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("discord_id");
            }
            return null;
        }
    }

    public List<String> getAllPlayers() {
        // Si la caché tiene datos, retornar desde ahí (más rápido)
        List<String> players = new ArrayList<>(whitelistedPlayers.size());
        for (String p : whitelistedPlayers) {
            players.add(p);
        }
        players.sort(java.util.Comparator.naturalOrder());
        return players;
    }

    /**
     * Verifica si un jugador está en la whitelist leyendo del caché (sin I/O).
     * Respeta la configuración whitelist.case-sensitive.
     * Alias de doesPlayerExist pero sin acceso a DB.
     */
    public boolean isWhitelisted(String username) {
        if (username == null) return false;
        boolean caseSensitive = getCollation().equals("BINARY");
        String key = caseSensitive ? username : username.toLowerCase();
        return whitelistedPlayers.contains(key);
    }

    /**
     * Retorna una copia inmutable del Set de whitelisteados (snapshot del caché).
     * Usado por LockdownCommand para evitar N+1 queries.
     */
    public Set<String> getWhitelistedPlayersCopy() {
        return Set.copyOf(whitelistedPlayers);
    }

    // ──────────────────────────── Shutdown ────────────────────────────

    public void closeConnection() {
        dbExecutor.shutdownNow();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error al cerrar la conexión a la base de datos: " + e.getMessage());
            }
        }
    }
}
