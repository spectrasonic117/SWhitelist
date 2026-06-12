package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class AddCommand {

    // Ejecutar comando add
    public static void execute(CommandSender sender, String player, Main plugin) {
        // Validar longitud del nombre del jugador
        if (player.length() < 3) {
            MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("error-limit"));
            return;
        }

        try {
            // Verificar si el jugador ya existe en la whitelist
            if (plugin.getDatabaseManager().doesPlayerExist(player)) {
                String message = plugin.getMessageManager().getMessage("already-exists", "player", player);
                MessageUtils.denyMessage(sender, message);
                return;
            }

            // Agregar jugador a la whitelist
            plugin.getDatabaseManager().addPlayer(player);
            String message = plugin.getMessageManager().getMessage("success-added", "player", player);
            MessageUtils.successMessage(sender, message);

            // Notificar a Discord
            if (plugin.getDiscordManager() != null) {
                plugin.getDiscordManager().notifyPlayerAdded(player, sender.getName());
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al agregar jugador a la whitelist: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }
}