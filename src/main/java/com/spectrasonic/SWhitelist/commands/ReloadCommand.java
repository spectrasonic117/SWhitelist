package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

public class ReloadCommand {

    // Ejecutar comando reload
    public static void execute(CommandSender sender, Main plugin) {
        try {
            // Recargar configuración
            plugin.getConfigManager().reloadConfig();
            
            // Recargar mensajes
            plugin.getMessageManager().reloadMessages();
            
            // Recargar Discord
            if (plugin.getDiscordManager() != null) {
                plugin.getDiscordManager().reload();
            }
            
            // Enviar mensaje de éxito
            MessageUtils.successMessage(sender, plugin.getMessageManager().getMessage("reload-success"));
            
            plugin.getLogger().info("Configuración recargada por " + sender.getName());

        } catch (Exception e) {
            plugin.getLogger().severe("Error al recargar configuración: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }
}
