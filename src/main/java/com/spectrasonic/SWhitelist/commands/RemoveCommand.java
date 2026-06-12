package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class RemoveCommand {

    // Ejecutar comando remove
    public static void execute(CommandSender sender, String player, Main plugin) {
        // Validar longitud del nombre del jugador
        if (player.length() < 3) {
            MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("error-limit"));
            return;
        }

        try {
            // Verificar si el jugador existe en la whitelist
            if (!plugin.getDatabaseManager().doesPlayerExist(player)) {
                String message = plugin.getMessageManager().getMessage("not-in-database", "player", player);
                MessageUtils.denyMessage(sender, message);
                return;
            }

            // Remover jugador de la whitelist
            plugin.getDatabaseManager().removePlayer(player);
            String message = plugin.getMessageManager().getMessage("success-removed", "player", player);
            MessageUtils.successMessage(sender, message);

            // Notificar a Discord
            if (plugin.getDiscordManager() != null) {
                plugin.getDiscordManager().notifyPlayerRemoved(player, sender.getName());
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al remover jugador de la whitelist: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }
}
