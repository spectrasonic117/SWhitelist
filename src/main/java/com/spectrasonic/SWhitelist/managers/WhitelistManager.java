package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Whitelist guardada en YAML: los nombres viven en whitelist.yml
 * y el estado on/off en config.yml (whitelist.enabled).
 * La lista se mantiene en memoria; cada escritura reescribe el archivo.
 */
@Getter
public class WhitelistManager {

    private final Main plugin;
    private final File file;

    // Snapshot inmutable: el login lo lee sin I/O ni bloqueos
    private volatile List<String> players = List.of();

    public WhitelistManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "whitelist.yml");
        if (!file.exists()) {
            plugin.saveResource("whitelist.yml", false);
        }
        reload();
    }

    // Recargar la lista desde disco
    public void reload() {
        players = List.copyOf(YamlConfiguration.loadConfiguration(file).getStringList("whitelist"));
    }

    public boolean isWhitelistEnabled() {
        return plugin.getConfig().getBoolean("whitelist.enabled", false);
    }

    public void setWhitelistEnabled(boolean enabled) {
        plugin.getConfig().set("whitelist.enabled", enabled);
        plugin.saveConfig();
    }

    // Coincidencia por nombre sin importar mayúsculas/minúsculas
    public boolean isWhitelisted(String username) {
        return username != null && players.stream().anyMatch(n -> n.equalsIgnoreCase(username));
    }

    public List<String> getAllPlayers() {
        return players.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public void addPlayer(String username) throws IOException {
        if (isWhitelisted(username)) {
            return;
        }
        List<String> updated = new ArrayList<>(players);
        updated.add(username);
        write(updated);
    }

    public void removePlayer(String username) throws IOException {
        List<String> updated = players.stream()
                .filter(n -> !n.equalsIgnoreCase(username))
                .toList();
        if (updated.size() != players.size()) {
            write(updated);
        }
    }

    private void write(List<String> updated) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("whitelist", updated);
        yaml.save(file);
        players = List.copyOf(updated);
    }
}
