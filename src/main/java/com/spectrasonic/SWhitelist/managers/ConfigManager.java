package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigManager {

    private final Main plugin;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // Cargar configuración
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // Recargar configuración
    public void reloadConfig() {
        loadConfig();
    }

    // Obtener configuración de lockdown
    public String getLockdownKickMessage() {
        return config.getString("lockdown.kick-message", "<red>El servidor está en modo lockdown.</red>");
    }

    public String getLockdownCountdownSound() {
        return config.getString("lockdown.countdown-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public String getLockdownKickMode() {
        return config.getString("lockdown.kick-mode", "notlisted");
    }

    // Obtener formatos de tiempo
    public String getTimeFormat(String unit) {
        return config.getString("formats." + unit, unit);
    }
}
