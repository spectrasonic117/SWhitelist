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
        return getString("prefix");
    }

    // Obtener configuración de base de datos
    public String getDatabaseFile() {
        return getString("database.file");
    }

    // Obtener configuración de lockdown
    public String getLockdownKickMessage() {
        return getString("lockdown.kick-message");
    }

    public String getLockdownCountdownSound() {
        return getString("lockdown.countdown-sound");
    }

    public String getLockdownKickMode() {
        return getString("lockdown.kick-mode");
    }

    // Obtener formatos de tiempo
    public String getTimeFormat(String unit) {
        return getString("formats." + unit, unit);
    }

    // Metodos de configuracion de Discord
    public boolean isDiscordEnabled() {
        return getBoolean("discord.enabled", false);
    }

    public String getDiscordBotToken() {
        return getString("discord.bot-token");
    }

    public String getDiscordGuildId() {
        return getString("discord.guild-id");
    }

    public String getDiscordChannelId() {
        return getString("discord.channel-id");
    }

    public java.util.List<String> getDiscordAdminRoles() {
        return getStringList("discord.roles.admin");
    }

    public java.util.List<String> getDiscordUserRoles() {
        return getStringList("discord.roles.user");
    }

    public boolean isDiscordNotificationEnabled(String key) {
        return getBoolean("discord.notifications." + key, true);
    }

    public String getDiscordEmbedColor(String key) {
        return getString("discord.embed." + key, "#22d2d4");
    }

    public String getDiscordEmbedFooter() {
        return getString("discord.embed.footer-text", "SWhitelist");
    }
}
