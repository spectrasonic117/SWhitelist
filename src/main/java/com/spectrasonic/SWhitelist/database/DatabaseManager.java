package com.spectrasonic.SWhitelist.database;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Fachada de almacenamiento de la whitelist.
 * Elige el backend según `database.type` (sqlite | h2) y mantiene
 * un caché en memoria para lecturas sin I/O (cada login, cada check).
 */
@Getter
public class DatabaseManager {

    private final Main plugin;
    private final WhitelistStorage storage;

    // ── In-memory caches (avoids per-login / per-player DB queries) ──
    private volatile boolean whitelistEnabled;
    private final Set<String> whitelistedPlayers = ConcurrentHashMap.newKeySet();

    // ── Single-thread executor for async DB writes ──
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SWhitelist-DB-Writer");
        t.setDaemon(true);
        return t;
    });

    public DatabaseManager(Main plugin) throws Exception {
        this.plugin = plugin;
        this.storage = createStorage();
        loadCache();
    }

    // ──────────────────────────── Storage selection ────────────────────────────

    private WhitelistStorage createStorage() throws Exception {
        String type = plugin.getConfigManager().getStorageType();
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        // Insensible a mayúsculas; cualquier valor desconocido cae en sqlite (comportamiento previo)
        if ("h2".equalsIgnoreCase(type) || "yml".equalsIgnoreCase(type)) {
            File whitelistFile = new File(dataFolder, "whitelist.yml");
            File configFile = new File(dataFolder, "config.yml");
            plugin.getLogger().config("Usando guardado whitelist.yml.");
            return new YamlStorage(whitelistFile, configFile, plugin.getLogger());
        }
        String file = plugin.getConfigManager().getDatabaseFile();
        File dbFile = new File(dataFolder, file != null ? file : "database.db");
        plugin.getLogger().config("Usando guardado SQLite (" + dbFile.getName() + ").");
        return new SqliteStorage(dbFile);
    }

    // ──────────────────────────── Cache loading ────────────────────────────

    /**
     * Carga el estado completo de la whitelist en memoria.
     * Se llama al iniciar y al recargar.
     */
    private void loadCache() {
        try {
            // Cargar estado enabled/disabled
            whitelistEnabled = storage.loadWhitelistEnabled();

            // Cargar todos los nombres en el Set de caché — siempre case-insensitive
            whitelistedPlayers.clear();
            for (String name : storage.loadPlayers()) {
                whitelistedPlayers.add(name.toLowerCase());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando caché de whitelist", e);
        }
    }

    /**
     * Recarga el caché completo desde el almacenamiento.
     * Llamar desde el comando /swhitelist reload.
     */
    public void reloadCache() {
        loadCache();
    }

    // ──────────────────────────── Cache-safe read methods (NO storage access) ────────────────────────────

    /**
     * Verifica si la whitelist está habilitada — lee del caché, NO del storage.
     */
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public void addPlayer(String username) throws Exception {
        addPlayer(username, null);
    }

    public void addPlayer(String username, String discordId) throws Exception {
        storage.addPlayer(username, discordId);
        whitelistedPlayers.add(username.toLowerCase());
    }

    public void removePlayer(String username) throws Exception {
        storage.removePlayer(username);
        whitelistedPlayers.remove(username.toLowerCase());
    }

    public boolean doesPlayerExist(String username) throws Exception {
        // Consulta el caché (siempre case-insensitive); si no está, podría
        // haberse añadido por fuera — el backend resuelve de última instancia.
        String key = username == null ? null : username.toLowerCase();
        if (whitelistedPlayers.contains(key)) {
            return true;
        }
        return storage.doesPlayerExist(username);
    }

    // ──────────────────────────── Async DB write helpers ────────────────────────────

    /**
     * Ejecuta un update de whitelist_enabled de forma asíncrona.
     * Actualiza el caché en el hilo actual para que las lecturas sean inmediatas,
     * y programa la escritura a disco en background.
     */
    public void setWhitelistEnabledAsync(boolean enabled) {
        // Actualizar caché inmediatamente (thread-safe)
        whitelistEnabled = enabled;
        // Escritura async al storage
        dbExecutor.submit(() -> {
            try {
                storage.saveWhitelistEnabled(enabled);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error actualizando whitelist_enabled en DB", e);
            }
        });
    }

    // ──────────────────────────── Data access (commands) ────────────────────────────

    public String getDiscordId(String username) throws Exception {
        return storage.getDiscordId(username);
    }

    public List<String> getAllPlayers() {
        List<String> players = new ArrayList<>(whitelistedPlayers.size());
        for (String p : whitelistedPlayers) {
            players.add(p);
        }
        players.sort(java.util.Comparator.naturalOrder());
        return players;
    }

    /**
     * Verifica si un jugador está en la whitelist leyendo del caché (sin I/O).
     * Coincidencia por nombre, sin importar mayúsculas/minúsculas.
     */
    public boolean isWhitelisted(String username) {
        if (username == null) return false;
        return whitelistedPlayers.contains(username.toLowerCase());
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
        if (storage != null) {
            try {
                storage.close();
            } catch (Exception e) {
                plugin.getLogger().severe("Error al cerrar la conexión a la base de datos: " + e.getMessage());
            }
        }
    }
}
