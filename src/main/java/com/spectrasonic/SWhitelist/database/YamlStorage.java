package com.spectrasonic.SWhitelist.database;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Backend YAML: la lista de jugadores vive en whitelist.yml.
 *
 * whitelist:
 * - player1
 * - player2
 *
 * Coincidencia por nombre sin importar mayúsculas/minúsculas.
 * El estado enabled vive en config.yml (database.enabled) y se
 * escribe ahí en cada on/off. El discord_id asignado por Discord
 * se mantiene en memoria y se preserva; no ensucia el yml. Sin
 * persistir, perdería el id al reiniciar — aceptado: el yml es
 * solo la lista de nombres.
 */
public class YamlStorage implements WhitelistStorage {

    private final File whitelistFile;
    private final Logger logger;
    private final File configFile;

    // discord_id por nombre (clave en minúsculas); vive solo en memoria
    private final Map<String, String> discordIds = new LinkedHashMap<>();

    public YamlStorage(File whitelistFile, File configFile, Logger logger) throws IOException {
        this.whitelistFile = whitelistFile;
        this.configFile = configFile;
        this.logger = logger;
        if (!whitelistFile.exists()) {
            File parent = whitelistFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            // Escribe la plantilla vacía (whitelist: []) del jar del plugin
            try (java.io.InputStream in = getClass().getResourceAsStream("/whitelist.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, whitelistFile.toPath());
                } else {
                    writeEmpty();
                }
            }
        }
    }

    private void writeEmpty() throws IOException {
        YamlConfiguration fresh = new YamlConfiguration();
        fresh.set("whitelist", new java.util.ArrayList<String>());
        fresh.save(whitelistFile);
    }

    // ─────────────────────────── whitelist.yml ───────────────────────────

    private YamlConfiguration loadYaml() {
        return YamlConfiguration.loadConfiguration(whitelistFile);
    }

    private void saveYaml(YamlConfiguration yaml) {
        try {
            yaml.save(whitelistFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "No se pudo guardar whitelist.yml", e);
        }
    }

    // ──────────────────────────── WhitelistStorage ────────────────────────────

    @Override
    public List<String> loadPlayers() {
        return loadYaml().getStringList("whitelist");
    }

    @Override
    public void addPlayer(String username, String discordId) throws IOException {
        if (discordId != null && !discordId.isEmpty()) {
            discordIds.put(username.toLowerCase(Locale.ROOT), discordId);
        }
        YamlConfiguration yaml = loadYaml();
        List<String> names = new ArrayList<>(yaml.getStringList("whitelist"));
        boolean exists = names.stream().anyMatch(n -> n.equalsIgnoreCase(username));
        if (!exists) {
            names.add(username);
            yaml.set("whitelist", names);
            saveYaml(yaml);
        }
    }

    @Override
    public void removePlayer(String username) throws IOException {
        discordIds.remove(username.toLowerCase(Locale.ROOT));
        YamlConfiguration yaml = loadYaml();
        List<String> names = yaml.getStringList("whitelist");
        boolean removed = names.removeIf(n -> n.equalsIgnoreCase(username));
        if (removed) {
            yaml.set("whitelist", names);
            saveYaml(yaml);
        }
    }

    @Override
    public boolean doesPlayerExist(String username) throws IOException {
        return loadYaml().getStringList("whitelist").stream()
                .anyMatch(n -> n.equalsIgnoreCase(username));
    }

    @Override
    public String getDiscordId(String username) throws IOException {
        return discordIds.get(username.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean loadWhitelistEnabled() {
        return YamlConfiguration.loadConfiguration(configFile)
                .getBoolean("whitelist.enabled", false);
    }

    @Override
    public void saveWhitelistEnabled(boolean enabled) throws Exception {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("whitelist.enabled", enabled);
        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "No se pudo guardar whitelist.enabled en config.yml", e);
        }
    }

    @Override
    public void close() throws Exception {
        // nada que cerrar
    }
}
