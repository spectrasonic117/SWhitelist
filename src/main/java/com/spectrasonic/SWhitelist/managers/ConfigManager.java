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

    // Guardar configuración
    public void saveConfig() {
        plugin.saveConfig();
    }

    // Obtener valor string
    public String getString(String key) {
        return config.getString(key);
    }

    // Obtener valor string con valor por defecto
    public String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    // Obtener valor int
    public int getInt(String key) {
        return config.getInt(key);
    }

    // Obtener valor int con valor por defecto
    public int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    // Obtener valor boolean
    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    // Obtener valor boolean con valor por defecto
    public boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    // Obtener lista de strings
    public java.util.List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    // Verificar si existe una clave
    public boolean contains(String key) {
        return config.contains(key);
    }

    // Obtener prefijo del plugin
    public String getPrefix() {
        return getString("prefix", "<gradient:#168788:#1e8e8e>lSWhitelist</gradient> <gray>»</gray> ");
    }

    // Obtener configuración de base de datos
    public String getDatabaseFile() {
        return getString("database.file", "swhitelist.db");
    }

    // Obtener configuración de lockdown
    public String getLockdownKickMessage() {
        return getString("lockdown.kick-message", "<red>El servidor está en modo lockdown.</red>");
    }

    public String getLockdownCountdownSound() {
        return getString("lockdown.countdown-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public String getLockdownKickMode() {
        return getString("lockdown.kick-mode", "notlisted");
    }

    // Obtener formatos de tiempo
    public String getTimeFormat(String unit) {
        return getString("formats." + unit, unit);
    }
}
