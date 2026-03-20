package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

@Getter
public class MessageManager {

    private final Main plugin;
    private FileConfiguration messages;

    public MessageManager(Main plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    // Cargar archivo de mensajes
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // Recargar mensajes
    public void reloadMessages() {
        loadMessages();
    }

    // Obtener mensaje como string
    public String getMessage(String key) {
        return messages.getString("message." + key, "<red>Mensaje no encontrado: " + key + "</red>");
    }

    // Obtener mensaje con placeholder
    public String getMessage(String key, String placeholder, String value) {
        String message = messages.getString("message." + key, "<red>Mensaje no encontrado: " + key + "</red>");
        return message.replace("%" + placeholder + "%", value);
    }

    // Obtener mensaje con múltiples placeholders
    public String getMessage(String key, String placeholder1, String value1, String placeholder2, String value2) {
        String message = messages.getString("message." + key, "<red>Mensaje no encontrado: " + key + "</red>");
        return message.replace("%" + placeholder1 + "%", value1)
                     .replace("%" + placeholder2 + "%", value2);
    }

    // Obtener lista de mensajes
    public List<String> getMessageList(String key) {
        return messages.getStringList("message." + key);
    }

    // Obtener prefijo del plugin
    public String getPrefix() {
        return getMessage("prefix");
    }
}
